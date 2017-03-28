package io.visualdig

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import io.visualdig.ClientTestHelper.Companion.assertMatchingMessage
import io.visualdig.Dig.Companion.searchEastOf
import io.visualdig.actions.*
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigTextQuery
import io.visualdig.exceptions.DigPreviousQueryFailedException
import io.visualdig.exceptions.DigSpacialException
import io.visualdig.exceptions.DigTextNotFoundException
import io.visualdig.exceptions.DigWebsiteException
import io.visualdig.results.*
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType
import io.visualdig.spacial.SearchPriority
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import java.net.MalformedURLException
import java.net.URI
import java.util.Arrays.asList
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

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.assertReceivedServerMessage()
        }

        test("invalid URI") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy({ dig!!.goTo(URI("ht1tp:badurl.com")) })
        }
    }

    describe("#findText") {
        test("text exists") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success,
                            digId = 12,
                            htmlId = "foo-html-id",
                            closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            assertThat(element.digId).isEqualTo(12)
            assertThat(element.htmlId).isEqualTo("foo-html-id")

            clientTestHelper!!.assertReceivedServerMessage()
        }

        test("text does not exist on page") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "fo text"),
                    send = FindTextResult(result = Result.Failure_NoMatch, digId = null, htmlId = null, closestMatches = listOf("foo text")),
                    assertionBlock = ::assertMatchingMessage)

            assertThatExceptionOfType(DigTextNotFoundException::class.java)
                    .isThrownBy { dig!!.findText("fo text") }
                    .withMessageContaining("Could not find the text 'fo text' when doing a find text query.")
                    .withMessageContaining("Did you possibly mean to search for 'foo text'?")
        }

        test("goTo hasn't been called yet") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            assertThatExceptionOfType(DigWebsiteException::class.java)
                    .isThrownBy { dig!!.findText("fo text") }
                    .withMessage("Call Dig.goTo before calling any query or interaction methods.")
        }
    }

    describe("#click") {
        test("click something found by text query") {

            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7,
                            htmlId = "foo-html-id", closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            val prevQueries = asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
                    textQuery = DigTextQuery("foo text"),
                    spacialQuery = null))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = ClickAction(digId = 7,
                            prevQueries = prevQueries),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            element.click()

            clientTestHelper!!.assertReceivedServerMessage()
        }

        test("element no longer exists") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7,
                            htmlId = "foo-html-id", closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            val prevQueries = asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
                    textQuery = DigTextQuery("foo text"),
                    spacialQuery = null))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = ClickAction(digId = 7,
                            prevQueries = prevQueries),
                    send = TestResult(result = Result.Failure_QueryExpired, message = "Could not find previously found text 'foo text'"),
                    assertionBlock = ::assertMatchingMessage)

            assertThatExceptionOfType(DigPreviousQueryFailedException::class.java)
                    .isThrownBy { element.click() }
        }
    }

    describe("#search") {
        test("search to the east starting at found text element") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7,
                            htmlId = "foo-html-id", closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            clientTestHelper!!.assertReceivedServerMessage()

            val prevQueries = asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
                    textQuery = DigTextQuery("foo text"),
                    spacialQuery = null))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = SpacialSearchAction(
                            direction = Direction.EAST,
                            elementType = ElementType.CHECKBOX,
                            digId = 7,
                            prevQueries = prevQueries,
                            toleranceInPixels = 20,
                            priority = SearchPriority.ALIGNMENT_THEN_DISTANCE
                    ),
                    send = SpacialSearchResult(result = Result.Success,
                            digId = 8,
                            closeResults = emptyList(), htmlId = "foo-html-id"),
                    assertionBlock = ::assertMatchingMessage)

            val elementAfterSearch = searchEastOf(element).forCheckbox()

            assertThat(elementAfterSearch.digId).isEqualTo(8)

            clientTestHelper!!.assertReceivedServerMessage()
        }

        test("search to the east and fail with a close match") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7,
                            htmlId = "foo-html-id", closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            clientTestHelper!!.assertReceivedServerMessage()

            val prevQueries = asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
                    textQuery = DigTextQuery("foo text"),
                    spacialQuery = null))

            val closestResult = CloseResult(100, 40, 22, "bar-id-1")
            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = SpacialSearchAction(
                            direction = Direction.EAST,
                            elementType = ElementType.CHECKBOX,
                            digId = 7,
                            prevQueries = prevQueries,
                            toleranceInPixels = 22,
                            priority = SearchPriority.ALIGNMENT_THEN_DISTANCE),
                    send = SpacialSearchResult(result = Result.Failure_NoMatch,
                            digId = null,
                            closeResults = asList(closestResult), htmlId = "foo-html-id"),
                    assertionBlock = ::assertMatchingMessage)

            assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { searchEastOf(element, tolerance = 22).forCheckbox() }
                    .withMessageContaining("Unable to find checkbox east of 'foo text' element.")
                    .withMessageContaining("The closest match was an element with id: bar-id-1.")
                    .withMessageContaining("The element was 18 pixels too far north to be considered aligned east.")
        }

        test("anchoring search element no longer exists") {
            verify(browserLauncher!!).launchBrowser(URI("http://localhost:8650/dig-it.html"), false)

            makeClientSession(clientTestHelper, container)

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = GoToAction(uri = "http://www.example.com/"),
                    send = TestResult(result = Result.Success, message = ""),
                    assertionBlock = ::assertMatchingMessage)

            dig!!.goTo(URI("http://www.example.com/"))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = FindTextAction(text = "foo text"),
                    send = FindTextResult(result = Result.Success, digId = 7,
                            htmlId = "foo-html-id", closestMatches = emptyList()),
                    assertionBlock = ::assertMatchingMessage)

            val element = dig!!.findText("foo text")

            val prevQueries = asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
                    textQuery = DigTextQuery("foo text"),
                    spacialQuery = null))

            clientTestHelper!!.whenReceiveAction(clientTestHelper!!,
                    expectedReceive = SpacialSearchAction(
                            direction = Direction.EAST,
                            elementType = ElementType.CHECKBOX,
                            digId = 7,
                            prevQueries = prevQueries,
                            toleranceInPixels = 20,
                            priority = SearchPriority.ALIGNMENT_THEN_DISTANCE),
                    send = SpacialSearchResult(result = Result.Failure_QueryExpired,
                            digId = null,
                            closeResults = emptyList(), htmlId = "foo-html-id"),
                    assertionBlock = ::assertMatchingMessage)


            assertThatExceptionOfType(DigPreviousQueryFailedException::class.java)
                    .isThrownBy { searchEastOf(element).forCheckbox() }
                    .withMessageContaining("Could not find previously found element")
        }
    }
})

private fun makeClientSession(clientTestHelper: ClientTestHelper?, container: WebSocketContainer?) {
    val session: Session = container!!.connectToServer(clientTestHelper!!, URI.create("ws://localhost:8650/dig"))
    while (!session.isOpen) {
        Thread.sleep(1)
        // wait until socket is open
    }
}