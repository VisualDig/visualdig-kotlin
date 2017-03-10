package io.virtualdig

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jdk.internal.org.objectweb.asm.TypeReference
import org.assertj.core.api.Assertions
import org.springframework.messaging.MessageDeliveryException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.websocket.ClientEndpoint
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import kotlin.properties.Delegates


@ClientEndpoint
class ClientTestHelper {
    data class ClientActionResult(val message: String?, val errorText: String?)
    companion object {
        fun assertMatchingMessage(result: ClientTestHelper.ClientActionResult) {
            if (result.errorText != null) {
                println(result.errorText)
                Assertions.assertThat(result.message).overridingErrorMessage(result.errorText).isNotNull()
            } else {
                Assertions.assertThat(result.message).isNotNull()
            }
        }
    }

    private var futureSession: CompletableFuture<Session> = CompletableFuture()
    var expectedHandler: (String) -> ClientActionResult = { ClientActionResult(null, "Action was never set up!") }
    var lastMessage: String by Delegates.observable("lastMessage") {
        _, _, new ->
            val result = expectedHandler.invoke(new)
            if (result.message != null) {
                sendMessage(result.message)
            }
    }

    fun sendMessage(message: String) {
        val session = currentSession()

        session.asyncRemote.sendText(message)
    }

    inline fun <reified T : Any, reified U : Any>
            whenReceiveAction(testHelper: ClientTestHelper,
                              expectedReceive: T,
                              send: U,
                              crossinline assertionBlock: (ClientActionResult) -> Unit,
                              delayInMs: Long = 0) {
        testHelper.expectedHandler = { message: String ->
            if(delayInMs > 0)
                Thread.sleep(delayInMs)

            val received: T = jacksonObjectMapper().readValue(message)
            if (received == expectedReceive) {
                val clientActionResult = ClientActionResult(message = jacksonObjectMapper().writeValueAsString(send), errorText = null)
                assertionBlock.invoke(clientActionResult)
                clientActionResult
            } else {
                val clientActionResult = ClientActionResult(message = null, errorText = "Expected object \n\n  $expectedReceive\n\nbut got this object instead \n\n  $received \n\n")
                assertionBlock.invoke(clientActionResult)
                clientActionResult
            }
        }
    }

    @OnOpen
    fun onOpen(session: Session) {
        this.futureSession.complete(session)
    }

    @OnMessage
    fun onMessage(message: String, session: Session) {
        val currSession = currentSession()

        if (currSession.id != session.id) throw Exception("Sessions do not match, we don't support multiple sessions at once")

        this.lastMessage = message
    }

    private fun currentSession() = futureSession.get(5, TimeUnit.SECONDS)
}
