package io.visualdig.actions

import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigTextQuery
import io.visualdig.exceptions.DigFatalException


data class ClickAction (
        override val digId: Int,
        override val prevQueries: List<ExecutedQuery>
) : ActionOnElementInterface, TestActionInterface {

    override val action: TestAction = TestAction(actionType())

    companion object {
        fun actionType() = "Click"

        fun createClickAction(digId: Int, prevQueries: List<ExecutedQuery>) : ClickAction {
            return ClickAction(digId = digId, prevQueries = prevQueries)
        }
    }
}
