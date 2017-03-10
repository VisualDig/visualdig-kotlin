package io.virtualdig

import java.io.File
import java.net.URI
import java.util.*
import java.util.Locale.ENGLISH

open class BrowserLauncher(val env : Map<String, String>, val commandRunner: CommandRunner, val operatingSystem: OperatingSystem) {
    private var browserProcess : Process? = null
    private val dirsToDelete : MutableList<File> = ArrayList()

    open fun launchBrowser(uri : URI, headless: Boolean = false) : Boolean {
        if(operatingSystem.isOSX()) {
            if(headless) throw NotImplementedError("Headless OSX is not implemented")

            val browserPath = env["DIG_BROWSER"] ?: "\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\""
            val url = uri.toURL().toExternalForm()
            if(browserPath.toLowerCase(ENGLISH).contains("firefox")) {
                return launchFirefox(browserPath, url)
            } else if(browserPath.toLowerCase(ENGLISH).contains("chrome")) {
                return launchChrome(browserPath, url)
            }
        } else if(operatingSystem.isLinux()) {
            var browserPath = env["DIG_BROWSER"] ?: "google-chrome"
            if(headless) {
               browserPath = "xvfb-run $browserPath"
            }
            val url = uri.toURL().toExternalForm()
            if(browserPath.toLowerCase(ENGLISH).contains("firefox")) {
                return launchFirefox(browserPath, url)
            } else if(browserPath.toLowerCase(ENGLISH).contains("chrome")) {
                return launchChrome(browserPath, url)
            }
        }

        return false
    }

    open fun stopBrowser() {
        Thread.sleep(100)
        browserProcess?.destroy()
        browserProcess = null
        dirsToDelete.forEach(File::deleteOnExit)
    }

    private fun launchFirefox(browserCommand: String?, url : String?) : Boolean {
        val dir = File("/")

        val command = "$browserCommand -safe-mode \"$url\""
        browserProcess = commandRunner.runCommand(command, dir)
        return true
    }


    private fun launchChrome(browserCommand: String?, url : String?) : Boolean {
        val dir = File("/")
        val random = Random().nextInt()
        dirsToDelete.add(File("/tmp/$random"))

        val switches = "--app=\"$url\" --no-first-run --user-data-dir=/tmp/$random --disk-cache-dir=/dev/null"
        browserProcess = commandRunner.runCommand("$browserCommand $switches", dir)
        return true
    }
}