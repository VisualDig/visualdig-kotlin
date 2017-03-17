package io.visualdig

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.websocket.ClientEndpoint
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import kotlin.properties.Delegates


@ClientEndpoint
class ClientTestHelper {
    data class ClientActionResult(val sentJson: String?, val sentinalErrorText: String?)
    companion object {
        fun assertMatchingMessage(result: ClientTestHelper.ClientActionResult) {
            if (result.sentinalErrorText != null) {
                println(result.sentinalErrorText)
                Assertions.assertThat(result.sentJson).overridingErrorMessage(result.sentinalErrorText).isNotNull()
            } else {
                Assertions.assertThat(result.sentJson).isNotNull()
            }
        }
    }

    private var futureSession: CompletableFuture<Session> = CompletableFuture()
    private var messageReceivedForHandler : Boolean = false
    var expectedHandler: (String) -> ClientActionResult by Delegates.observable({a -> ClientActionResult(null, "Action was never set up!") }) {
        _, _, new ->
            messageReceivedForHandler = false
    }

    var lastMessage: String by Delegates.observable("lastMessage") {
        _, _, new ->
            messageReceivedForHandler = true
            val result = expectedHandler.invoke(new)
            if (result.sentJson != null) {
                sendMessage(result.sentJson)
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
                val clientActionResult = ClientActionResult(sentJson = jacksonObjectMapper().writeValueAsString(send),
                                                            sentinalErrorText = null)
                assertionBlock.invoke(clientActionResult)
                clientActionResult
            } else {
                val clientActionResult = ClientActionResult(sentJson = null,
                                                            sentinalErrorText = "Expected object \n\n  $expectedReceive\n\nbut got this object instead \n\n  $received \n\n")
                assertionBlock.invoke(clientActionResult)
                clientActionResult
            }
        }
    }

    fun assertReceivedServerMessage() {
        Assertions.assertThat(messageReceivedForHandler)
                .overridingErrorMessage("Expected to receive a message from the Dig Websocket server but none ever arrived!")
                .isTrue()
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
