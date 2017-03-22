package io.visualdig.results

sealed class ResponseWrapper {
    data class BadMessage(val jsonMessage: String) : ResponseWrapper()
    data class Success<out T>(val result: T): ResponseWrapper()
}