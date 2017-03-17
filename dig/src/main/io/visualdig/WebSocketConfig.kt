package io.visualdig


import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@SpringBootApplication
@Configuration
@EnableWebSocket
@EnableAutoConfiguration(exclude = arrayOf(DataSourceAutoConfiguration::class))
open class WebSocketConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(digHandler(), "/dig").setAllowedOrigins("*")
    }

    @Bean
    open fun digHandler(): WebSocketHandler {
        return DigController()
    }
}