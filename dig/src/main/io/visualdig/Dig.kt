package io.visualdig

import io.visualdig.element.DigTextQuery
import io.visualdig.element.DigWebElement
import io.visualdig.exceptions.DigFatalException
import io.visualdig.spacial.DigSpacialSearch
import io.visualdig.spacial.Direction
import io.visualdig.spacial.SearchPriority
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.net.URI
import java.util.Arrays.asList
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Dig(
        val digHtmlTestFile: URI,
        val browserLauncher: BrowserLauncher = BrowserLauncher(System.getenv(), CommandRunner(), OperatingSystem()),
        overrideController: DigController? = null
) {
    private val context: CompletableFuture<ConfigurableApplicationContext> = CompletableFuture()
    private val controller: CompletableFuture<DigController> = CompletableFuture()

    init {
        if (overrideController == null) {
            start(arrayOf("--server.port=8650"))
        } else {
            this.controller.complete(overrideController)
        }
    }

    fun goTo(uri: URI) {
        digController().goTo(uri)
    }

    fun findText(text: String): DigWebElement {
        val result = digController().find(DigTextQuery(text))

        if (result.digId == null)
            throw DigFatalException("DigTextNotFoundException was not thrown, and execution continued when it should not have.")

        return DigWebElement(result.digId, result.htmlId, asList(DigTextQuery(text)), digController())
    }

    fun close() {
        if (this.context.isDone) {
            this.context.get(5, TimeUnit.SECONDS).close()
            Thread.sleep(100)
            browserLauncher.stopBrowser()
        }
    }

    companion object {
        fun searchEastOf(elem: DigWebElement,
                         tolerance : Int = 20,
                         priority : SearchPriority = SearchPriority.ALIGNMENT_THEN_DISTANCE): DigSpacialSearch {
            return elem.spacialSearch(
                    direction = Direction.EAST,
                    toleranceInPixels = tolerance,
                    searchPriority = priority)
        }
    }

    private fun digController() = this.controller.get(5, TimeUnit.SECONDS)

    private fun start(args: Array<String>) {
        val context: ConfigurableApplicationContext = SpringApplication.run(WebSocketConfig::class.java, *args)
        this.context.complete(context)
        this.controller.complete(context.getBean(DigController::class.java))
        if (!SocketWaiter("localhost", 8650).wait(maxNumberOfRetries = 40)) {
            throw DigFatalException("Failed to boot up the Dig websockets server after retrying many times.")
        }
        browserLauncher.launchBrowser(digHtmlTestFile, false)
    }

}
