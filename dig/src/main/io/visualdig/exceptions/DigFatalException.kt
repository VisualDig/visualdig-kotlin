package io.visualdig.exceptions

class DigFatalException(message: String)
    : Exception(getDetailedMessage(message)) {

    companion object {
        private fun getDetailedMessage(message : String) : String {
            val errorText = """
$message

This is an error in the Dig framework and any stacktraces should be sent to the maintainer.
"""
            return errorText
        }
    }
}