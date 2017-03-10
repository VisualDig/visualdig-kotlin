package io.virtualdig

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import io.virtualdig.ClientTestHelper.Companion.assertMatchingMessage
import io.virtualdig.exceptions.DigWebsiteException
import org.assertj.core.api.Assertions.*
import java.net.MalformedURLException
import java.net.URI
import javax.websocket.ContainerProvider
import javax.websocket.Session
import javax.websocket.WebSocketContainer


class DigSocketTest : Test({
    var container: WebSocketContainer? = null
    var clientTestHelper: ClientTestHelper? = null
    var browserLauncher : BrowserLauncher? = null
    var dig: Dig? = null

    before {
        container = ContainerProvider.getWebSocketContainer()
        clientTestHelper = ClientTestHelper()
        browserLauncher = mock()
        whenever(browserLauncher!!.launchBrowser(any(), any())).thenReturn(true)
        dig = Dig(browserLauncher!!)
    }

    after {
        container = null
        clientTestHelper = null
        dig?.close()
    }

    describe("#goTo") {
        test("valid URI") {

            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = "Success", message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))
        }

        test("invalid URI") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy({ dig!!.goTo(URI("ht1tp:badurl.com")) })
        }

        test("show failure from browser runner") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = "Failure", message = "Could not open website requested"),
                    assertionBlock = ::assertMatchingMessage,
                    delayInMs = 1000)

            assertThatExceptionOfType(DigWebsiteException::class.java).isThrownBy({
                dig!!.goTo(URI("http://www.example.com/"))
            })
        }
    }
})
/**/