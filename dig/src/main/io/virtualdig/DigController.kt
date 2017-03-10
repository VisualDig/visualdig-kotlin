package io.virtualdig


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.virtualdig.exceptions.DigWebsiteException
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
    private val futureSession: CompletableFuture<WebSocketSession> = CompletableFuture()
    private fun webSocketSession() = futureSession.get(5, TimeUnit.SECONDS)

    var messageListeners: MutableList<(String) -> Unit> = mutableListOf()
    fun listenToNextMessage(handler: (String) -> Unit) {
        synchronized(messageListeners) {
            messageListeners.add(handler)
        }
    }

    var message: String by Delegates.observable("latestMessage") {
        _, _, new ->
        synchronized(messageListeners) {
            messageListeners.forEach { it(new) }
            messageListeners.clear()
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

        val (result, testMessage) = resultWaiter.get(10, TimeUnit.SECONDS)

        if (result == "Failure") throw DigWebsiteException("Browser failed to go to URL: $urlString\n\n$testMessage")
    }


    fun clickLink(withText: String, withId: String? = null) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}