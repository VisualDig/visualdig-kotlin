package io.virtualdig.featuretest

import io.damo.aspen.Test
import io.virtualdig.Dig
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
        dig = Dig()
        Thread.sleep(2000)
        dig?.goTo(URI("http://localhost:9292/testpage.html"))
    }
})