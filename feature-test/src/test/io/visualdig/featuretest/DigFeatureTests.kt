package io.visualdig.featuretest

import io.damo.aspen.Test
import io.visualdig.Dig
import io.visualdig.Dig.Companion.searchEastOf
import io.visualdig.exceptions.DigSpacialException
import io.visualdig.exceptions.DigTextNotFoundException
import io.visualdig.spacial.SearchPriority
import org.assertj.core.api.Assertions.assertThat
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

    test("tests searchPriority") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        dig!!.goTo(URI("http://localhost:9292/testpage.html"))

        val dtaAnchor = dig!!.findText("DistanceThenAlignment")
        val dtaCheckbox = searchEastOf(dtaAnchor,
                priority = SearchPriority.DISTANCE_THEN_ALIGNMENT).forCheckbox()
        assertThat(dtaCheckbox.htmlId).isEqualTo("dta-B")

        val atdAnchor = dig!!.findText("AlignmentThenDistance")
        val atdCheckbox = searchEastOf(atdAnchor,
                priority = SearchPriority.ALIGNMENT_THEN_DISTANCE).forCheckbox()
        assertThat(atdCheckbox.htmlId).isEqualTo("atd-B")

        val doAnchor = dig!!.findText("DistanceOnly")
        val doCheckbox = searchEastOf(doAnchor,
                priority = SearchPriority.DISTANCE).forCheckbox()
        assertThat(doCheckbox.htmlId).isEqualTo("do-C")

        val aoAnchor = dig!!.findText("AlignmentOnly")
        val aoCheckbox = searchEastOf(aoAnchor,
                priority = SearchPriority.ALIGNMENT).forCheckbox()
        assertThat(aoCheckbox.htmlId).isEqualTo("ao-B")
    }
})