package io.virtualdig


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.virtualdig.actions.GoToAction
import io.virtualdig.element.DigTextQuery
import io.virtualdig.exceptions.DigTextNotFoundException
import io.virtualdig.exceptions.DigWebsiteException
import io.virtualdig.results.FindTextResult
import io.virtualdig.results.Result
import io.virtualdig.results.TestResult
import io.virtualdig.results.isFailure
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
            throw Exception("Session ids do not match. VirtualDig does not support multiple websocket connections at once")
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
            throw Exception("Session ids do not match. VirtualDig does not support multiple websocket connections at once")
        }

        if (message.payload == null) {
            throw Exception("Response was empty, something went wrong")
        }

        this.message = message.payload
        val payload = message.payload
        println("RECEIVED MESSAGE $payload")
    }

    fun goTo(uri: URI) {
        val resultWaiter: CompletableFuture<TestResult> = CompletableFuture()
        listenToNextMessage({ message ->
            val result: TestResult = jacksonObjectMapper().readValue(message)
            resultWaiter.complete(result)
        })

        val session: WebSocketSession = webSocketSession() ?: throw Exception("No session exists yet")

        val url: URL = uri.toURL() ?: throw Exception("URI provided was invalid")

        val urlString = url.toExternalForm()

        val goToAction = GoToAction(uri = urlString)
        session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(goToAction)))

        val (result, testMessage) = resultWaiter.get(5, TimeUnit.SECONDS)

        if (result.isFailure()) throw DigWebsiteException("Browser failed to go to URL: $urlString\n\n$testMessage")

        initialized = true
    }

    fun find(digTextQuery: DigTextQuery) {
        if(!initialized) {
            throw DigWebsiteException("Call Dig.goTo before calling any query or interaction methods.")
        }

        val resultWaiter: CompletableFuture<FindTextResult> = CompletableFuture()
        listenToNextMessage({ message ->
            val result: FindTextResult = jacksonObjectMapper().readValue(message)
            resultWaiter.complete(result)
        })

        val session: WebSocketSession = webSocketSession() ?: throw Exception("No session exists yet")

        session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(digTextQuery.specificAction())))

        val (result, closestText) = resultWaiter.get(5, TimeUnit.SECONDS)

        if (result.isFailure()) throw DigTextNotFoundException(digTextQuery, closestText.first())
    }
}