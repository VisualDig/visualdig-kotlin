package io.virtualdig.actions

import io.virtualdig.element.DigElementQuery
import io.virtualdig.element.DigTextQuery
import io.virtualdig.exceptions.DigFatalException


data class ClickAction (
        override val digId: Int,
        override val usedQueryType: String,
        override val usedTextQuery: DigTextQuery? = null
) : ActionOnElementInterface, TestActionInterface {

    override val action: TestAction = TestAction(ClickAction.actionType())

    companion object {
        fun actionType() = "Click"

        fun createClickAction(digId: Int, query: DigElementQuery) : ClickAction {
            val actionType = query.action().action.actionType
            when(actionType) {
                FindTextAction.actionType() -> {
                    val textQuery = query as DigTextQuery
                    return ClickAction(digId = digId,
                                       usedQueryType = DigTextQuery.queryType(),
                                       usedTextQuery = textQuery)
                }
                else -> {
                    throw DigFatalException("Unable to resolve action type $actionType while creating ClickAction")
                }
            }
        }
    }
}
