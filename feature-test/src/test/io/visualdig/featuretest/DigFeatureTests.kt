package io.visualdig.featuretest

import io.damo.aspen.Test
import io.visualdig.Dig
import io.visualdig.Dig.Companion.searchEastOf
import io.visualdig.exceptions.DigSpacialException
import io.visualdig.exceptions.DigTextNotFoundException
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
        searchEastOf(element).forCheckbox()

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

    test("finds a closeResult checkbox") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        dig!!.goTo(URI("http://localhost:9292/testpage.html"))

        val element = dig!!.findText("Nothing right of here")

        assertThatExceptionOfType(DigSpacialException::class.java)
                .isThrownBy { searchEastOf(element).forCheckbox() }
                .withMessageContaining("Unable to find checkbox east of 'Nothing right of here' element.")
                .withMessageContaining("The closest match was an element with id: checkbox-jolt.")
                .withMessageContaining("The element was 1 pixels too far north to be considered aligned east.")
    }
})