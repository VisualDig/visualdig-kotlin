package io.virtualdig

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import io.virtualdig.ClientTestHelper.Companion.assertMatchingMessage
import io.virtualdig.actions.FindTextAction
import io.virtualdig.actions.GoToAction
import io.virtualdig.actions.TestAction
import io.virtualdig.exceptions.DigTextNotFoundException
import io.virtualdig.exceptions.DigWebsiteException
import io.virtualdig.results.FindTextResult
import io.virtualdig.results.Result
import io.virtualdig.results.TestResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import java.net.MalformedURLException
import java.net.URI
import javax.websocket.ContainerProvider
import javax.websocket.Session
import javax.websocket.WebSocketContainer


class DigSocketTest : Test({
    var container: WebSocketContainer? = null
    var clientTestHelper: ClientTestHelper? = null
    var browserLauncher: BrowserLauncher? = null
    var dig: Dig? = null

    before {
        container = ContainerProvider.getWebSocketContainer()
        clientTestHelper = ClientTestHelper()
        browserLauncher = mock()
        whenever(browserLauncher!!.launchBrowser(any(), any())).thenReturn(true)
        dig = Dig(digHtmlTestFile = URI("http://localhost:8650/dig-it.html"), browserLauncher = browserLauncher!!)
    }

    after {
        container = null
        clientTestHelper = null
        dig?.close()
    }

    describe("#goTo") {
        test("valid URI") {

            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))
        }

        test("invalid URI") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy({ dig!!.goTo(URI("ht1tp:badurl.com")) })
        }

        test("show failure from browser runner") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Failure, message = "Could not open website requested"),
                    assertionBlock = ::assertMatchingMessage,
                    delayInMs = 1000)

            assertThatExceptionOfType(DigWebsiteException::class.java).isThrownBy({
                dig!!.goTo(URI("http://www.example.com/"))
            })
        }
    }

    describe("#findText") {
        test("text exists") {

            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.findText("foo text")
        }

        test("text does not exist on page") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(action = TestAction(actionType = "GoTo"), uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "fo text"),
                    send = FindTextResult(result = Result.Failure, closestMatches = listOf("foo text")),
                    assertionBlock = ::assertMatchingMessage)

            try {
                dig!!.findText("fo text")
            } catch(e : DigTextNotFoundException){
                assertThat(e).hasMessageContaining("Could not find the text 'fo text' when doing a find text query.")
                assertThat(e).hasMessageContaining("Did you possibly mean to search for 'foo text'?")
            }
        }

        test("goTo hasn't been called yet") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            try {
                dig!!.findText("fo text")
            } catch(e : DigWebsiteException){
                assertThat(e).hasMessageContaining("Call Dig.goTo before calling any query or interaction methods.")
            }
        }
    }
})
/**/