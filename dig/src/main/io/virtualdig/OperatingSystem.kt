package io.virtualdig

import java.util.Locale.ENGLISH


open class OperatingSystem {
    open fun isOSX() : Boolean {
        return osPropertyString().indexOf("mac") >= 0 || osPropertyString().indexOf("darwin") >= 0
    }

    open fun isLinux() : Boolean {
        return osPropertyString().indexOf("nux") >= 0
    }

    open fun isWindows() : Boolean {
        return osPropertyString().indexOf("win") >= 0
    }

    private fun osPropertyString() = System.getProperty("os.name").toLowerCase(ENGLISH)
}