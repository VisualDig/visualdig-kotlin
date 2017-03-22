package io.visualdig


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.visualdig.actions.ActionOnElementInterface
import io.visualdig.actions.ClickAction
import io.visualdig.actions.ExecutedQuery.Companion.createExecutedQuery
import io.visualdig.actions.GoToAction
import io.visualdig.actions.SpacialSearchAction
import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigTextQuery
import io.visualdig.element.DigWebElement
import io.visualdig.exceptions.*
import io.visualdig.results.*
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.reflect.KClass

@Scope(SCOPE_SINGLETON)
class DigController : TextWebSocketHandler() {
    private var initialized: Boolean = false

    private val futureSession: CompletableFuture<WebSocketSession> = CompletableFuture()
    private fun webSocketSession() = futureSession.get(5, TimeUnit.SECONDS)

    var messageListeners: MutableList<(String) -> Unit> = mutableListOf()
    var message: String by Delegates.observable("latestMessage") {
        _, _, new ->
        synchronized(messageListeners) {
            messageListeners.forEach { it(new) }
            messageListeners.clear()
        }
    }

    fun listenToNextMessage(handler: (String) -> Unit) {
        synchronized(messageListeners) {
            messageListeners.add(handler)
        }
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        if (futureSession.isDone && futureSession.get().id != session.id) {
            throw Exception("Session ids do not match. VisualDig does not support multiple websocket connections at once")
        }

        if (!futureSession.isDone) {
            futureSession.complete(session)
        }
    }

    override fun handleTextMessage(session: WebSocketSession?, message: TextMessage?) {
        if (message == null) {
            throw Exception("Message was null in the main websocket hanndler!")
        }
        if (session == null) {
            throw Exception("Session was null in the main websocket hanndler!")
        }

        if (futureSession.isDone && futureSession.get().id != session.id) {
            throw Exception("Session ids do not match. VisualDig does not support multiple websocket connections at once")
        }

        if (message.payload == null) {
            throw Exception("Response was empty, something went wrong")
        }

        this.message = message.payload
    }

    fun goTo(uri: URI) {
        val url: URL = uri.toURL() ?: throw Exception("URI provided was invalid")
        val urlString = url.toExternalForm()
        val goToAction = GoToAction(uri = urlString)

        val validResult = sendAndReceive(TestResult::class, goToAction, this::objectMap)

        if (validResult.result.isFailure()) {
            val message = validResult.message
            throw DigWebsiteException("Browser failed to go to URL: $urlString\n\n$message")
        }

        initialized = true
    }

    fun find(digTextQuery: DigTextQuery): FindTextResult {
        if (!initialized) {
            throw DigWebsiteException("Call Dig.goTo before calling any query or interaction methods.")
        }

        val validResult = sendAndReceive(FindTextResult::class, digTextQuery.specificAction(), this::objectMap)

        if (validResult.result.isFailure())
            throw DigTextNotFoundException(digTextQuery, validResult.closestMatches.first())

        return validResult
    }

    fun click(digId: Int, prevQueries: List<DigElementQuery>) {
        if (!initialized) {
            throw DigWebsiteException("Call Dig.goTo before calling any queryUsed or interaction methods.")
        }

        val action = ClickAction.createClickAction(digId, prevQueries.map(::createExecutedQuery))

        val validResult = sendAndReceive(TestResult::class, action, this::objectMap)

        if (validResult.result.isFailure() && action.prevQueries.isNotEmpty()) {
            throw DigPreviousQueryFailedException("Could not find previously found element, TODO more error message.")
        }
    }

    fun search(action: SpacialSearchAction): SpacialSearchResult {
        if (!initialized) {
            throw DigWebsiteException("Call Dig.goTo before calling any action or interaction methods.")
        }

        val validResult : SpacialSearchResult = sendAndReceive(SpacialSearchResult::class, action, this::objectMap)

        if (validResult.result.isFailure()) {
            if (validResult.message.isNotEmpty()) {
                throw DigPreviousQueryFailedException("Could not find previously found element, TODO more error message.")
            } else {
                throw DigSpacialException(action, validResult)
            }
        }

        return validResult
    }

    private inline fun <reified R : Any> objectMap(text:String) : R {
        return jacksonObjectMapper().readValue(text)
    }

    private fun <A, R : Any> sendAndReceive(resultType: KClass<R>, action: A, method: (String) -> R): R {
        val resultWaiter: CompletableFuture<ResponseWrapper> = CompletableFuture()
        listenToNextMessage({ message ->
            try {
                val obj: R = method(message)
                resultWaiter.complete(ResponseWrapper.Success(obj))
            } catch (e: Exception) {
                println(e.message)
                resultWaiter.complete(ResponseWrapper.BadMessage(message))
            }
        })

        val session: WebSocketSession = webSocketSession() ?: throw Exception("No session exists yet")

        session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(action)))

        val testResult = resultWaiter.get(5, TimeUnit.SECONDS)

        when (testResult) {
            is ResponseWrapper.BadMessage -> {
                val jsonMessage = testResult.jsonMessage
                val className = resultType.simpleName
                throw DigFatalException("""
    Expected $className message but was unable to parse it.

    JSON message received from elm:

    $jsonMessage""")
            }
            is ResponseWrapper.Success<*> -> {
                return testResult.result as R
            }
        }
    }
}