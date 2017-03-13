package io.virtualdig.exceptions

import io.virtualdig.element.DigTextQuery

class DigTextNotFoundException(query: DigTextQuery, closestText: String)
    : Exception(getDetailedMessage(query, closestText)) {

    companion object {
        private fun getDetailedMessage(query : DigTextQuery, closestText: String) : String {
            val queryText = query.text
            val errorText = """
Could not find the text '$queryText' when doing a find text query.

Did you possibly mean to search for '$closestText'?
"""
            return errorText
        }
    }
}