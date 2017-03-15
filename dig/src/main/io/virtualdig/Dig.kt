package io.virtualdig

import io.virtualdig.element.DigTextQuery
import io.virtualdig.element.DigWebElement
import io.virtualdig.exceptions.DigFatalException
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Dig(
        val digHtmlTestFile: URI,
        val browserLauncher: BrowserLauncher = BrowserLauncher(System.getenv(), CommandRunner(), OperatingSystem())
) {
    private val context: CompletableFuture<ConfigurableApplicationContext> = CompletableFuture()
    private val controller: CompletableFuture<DigController> = CompletableFuture()

    init {
        start(arrayOf("--server.port=8650"))
    }

    fun goTo(uri: URI) {
        digController().goTo(uri)
    }

    fun findText(text: String): DigWebElement {
        val result = digController().find(DigTextQuery(text))

        if(result.digId == null)
            throw DigFatalException("DigTextNotFoundException was not thrown, and execution continued when it should not have.")

        return DigWebElement(result.digId, DigTextQuery(text), digController())
    }

    fun close() {
        this.context.get(5, TimeUnit.SECONDS).close()
        Thread.sleep(100)
        browserLauncher.stopBrowser()
    }

    private fun digController() = this.controller.get(5, TimeUnit.SECONDS)

    private fun start(args: Array<String>) {
        val context: ConfigurableApplicationContext = SpringApplication.run(WebSocketConfig::class.java, *args)
        this.context.complete(context)
        this.controller.complete(context.getBean(DigController::class.java))
        browserLauncher.launchBrowser(digHtmlTestFile, false)
    }

}