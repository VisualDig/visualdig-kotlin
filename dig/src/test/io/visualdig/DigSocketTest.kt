package io.visualdig

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import io.visualdig.ClientTestHelper.Companion.assertMatchingMessage
import io.visualdig.actions.ClickAction
import io.visualdig.actions.FindTextAction
import io.visualdig.actions.GoToAction
import io.visualdig.actions.TestAction
import io.visualdig.element.DigTextQuery
import io.visualdig.element.DigWebElement
import io.visualdig.exceptions.DigPreviousQueryFailedException
import io.visualdig.exceptions.DigTextNotFoundException
import io.visualdig.exceptions.DigWebsiteException
import io.visualdig.results.FindTextResult
import io.visualdig.results.Result
import io.visualdig.results.TestResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import java.net.MalformedURLException
import java.net.URI
import javax.websocket.ContainerProvider
import javax.websocket.Session
import javax.websocket.WebSocketContainer
import java.io.IOException

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
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.assertReceivedServerMessage()
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
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
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
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 12, closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            assertThat(element.digId).isEqualTo(12)

            val textQuery: DigTextQuery = element.queryUsed as DigTextQuery
            assertThat(textQuery.text).isEqualTo("foo text")

            clientTestHelper!!.assertReceivedServerMessage()
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
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "fo text"),
                    send = FindTextResult(result = Result.Failure, digId = null, closestMatches = listOf("foo text")),
                    assertionBlock = ::assertMatchingMessage)

            assertThatExceptionOfType(DigTextNotFoundException::class.java)
                    .isThrownBy { dig!!.findText("fo text") }
                    .withMessageContaining("Could not find the text 'fo text' when doing a find text query.")
                    .withMessageContaining("Did you possibly mean to search for 'foo text'?")
        }

        test("goTo hasn't been called yet") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            assertThatExceptionOfType(DigWebsiteException::class.java)
                    .isThrownBy { dig!!.findText("fo text") }
                    .withMessage("Call Dig.goTo before calling any query or interaction methods.")
        }
    }

    describe("#click") {
        test("click something found by text query") {

            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7, closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = ClickAction(digId = 7,
                            usedQueryType = DigTextQuery.queryType(),
                            usedTextQuery = DigTextQuery("foo text")),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            element.click()

            clientTestHelper!!.assertReceivedServerMessage()
        }

        test("element no longer exists") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
            while (!session.isOpen) {
                Thread.sleep(1)
                // sometime it is doesn't work, but I dont know solution of this problem
                // wait until socket is open
            }

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7, closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = ClickAction(digId = 7,
                            usedQueryType = DigTextQuery.queryType(),
                            usedTextQuery = DigTextQuery("foo text")),
                    send = TestResult(result = Result.Failure, message = "Could not find previously found text 'foo text'"),
                    assertionBlock = ::assertMatchingMessage)

            assertThatExceptionOfType(DigPreviousQueryFailedException::class.java)
                    .isThrownBy { element.click() }
                    .withMessage("Could not find previously found text 'foo text'.")
        }
    }
})