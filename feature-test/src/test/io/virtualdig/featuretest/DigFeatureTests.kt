package io.virtualdig.featuretest

import io.damo.aspen.Test
import io.virtualdig.Dig
import io.virtualdig.exceptions.DigTextNotFoundException
import org.assertj.core.api.Assertions
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
    test("website loads") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        Thread.sleep(2000)
        dig?.goTo(URI("http://localhost:9292/testpage.html"))
        dig?.findText("Gloogorb")
    }

    test("website loads and doesn't find text") {
        dig = Dig(digHtmlTestFile = URI("http://localhost:9292/dig-it.html"))
        Thread.sleep(2000)
        dig?.goTo(URI("http://localhost:9292/testpage.html"))
        try {
            dig?.findText("Galoog")
        } catch(e : DigTextNotFoundException){
            Assertions.assertThat(e).hasMessageContaining("Could not find the text 'Galoog' when doing a find text query.")
            Assertions.assertThat(e).hasMessageContaining("Did you possibly mean to search for 'Gloogorb'?")
        }
    }
})