package io.virtualdig.featuretest

import io.damo.aspen.Test
import io.virtualdig.Dig
import io.virtualdig.exceptions.DigTextNotFoundException
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.net.URI

class DigFeatureTests : Test({
    var applicationContext : ConfigurableApplicationContext? = null
    var dig : Dig? = null
    before {
        applicationContext = SpringApplication.run(Application::class.java, "--server.port=9292")
    }

    after {
        applicationContext?.close()
        dig?.close()
    }

    test("happy path testing") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        dig?.goTo(URI("http://localhost:9292/testpage.html"))

        val element = dig!!.findText("Jolt")
        element.click()

        dig!!.findText("jolted!")
    }

    test("website loads and doesn't find text") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        dig?.goTo(URI("http://localhost:9292/testpage.html"))

        assertThatExceptionOfType(DigTextNotFoundException::class.java)
                .isThrownBy { dig?.findText("galactic") }
                .withMessageContaining("Could not find the text 'galactic' when doing a find text query.")
                .withMessageContaining("Did you possibly mean to search for 'GALACTIC'?")
    }
})