package io.virtualdig

import org.apache.coyote.http2.ConnectionException
import java.net.Socket

class SocketWaiter(val host : String, val port : Int) {
    fun wait(maxNumberOfRetries : Int): Boolean {
        var retryCounter = 0

        while (retryCounter < maxNumberOfRetries) {
            try {
                retryCounter++
                val sock = Socket(host, port)
                sock.close()
                Thread.sleep(50)
                return true
            } catch (e : ConnectionException) {
                Thread.sleep(200)
            }
        }

        return false
    }
}