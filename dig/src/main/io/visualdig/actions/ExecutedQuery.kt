package io.visualdig.actions

import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigTextQuery
import io.visualdig.exceptions.DigFatalException

data class ExecutedQuery (val queryType: String,
                          val textQuery: DigTextQuery?,
                          val spacialQuery: DigSpacialQuery?) {

    fun queryDescription() : String {
        when(queryType) {
            DigTextQuery.queryType() -> {
                if(textQuery != null) {
                    val text = textQuery.text
                    return "'$text' element"
                }
                throw DigFatalException("For \"$queryType\" the object expected textQuery field was null")
            }
            DigSpacialQuery.queryType() -> {
                if(spacialQuery != null) {
                    val elementType = spacialQuery.elementType.description
                    return "spacially found $elementType"
                }
                throw DigFatalException("For \"$queryType\" the object expected spacialSearch field was null")
            }
            else -> {
                throw DigFatalException("Unable to resolve action type \"$queryType\" while generating query description")
            }
        }

    }

    companion object {
        fun createExecutedQuery(query : DigElementQuery) : ExecutedQuery {
            val actionType = query.action().action.actionType
            when(actionType) {
                FindTextAction.actionType() -> {
                    val textQuery = query as DigTextQuery
                    return ExecutedQuery(queryType = DigTextQuery.queryType(),
                            textQuery = textQuery,
                            spacialQuery = null)
                }
                SpacialSearchAction.actionType() -> {
                    val spacialQuery = query as DigSpacialQuery
                    return ExecutedQuery(queryType = DigSpacialQuery.queryType(),
                            textQuery = null,
                            spacialQuery = spacialQuery)
                }
                else -> {
                    throw DigFatalException("Unable to resolve action type \"$actionType\" while creating executed query")
                }
            }
        }
    }
}

