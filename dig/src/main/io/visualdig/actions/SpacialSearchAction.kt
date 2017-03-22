package io.visualdig.actions

import io.visualdig.element.DigSpacialQuery
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType

data class SpacialSearchAction(
        val direction : Direction,
        val elementType : ElementType,
        override val digId: Int,
        override val prevQueries: List<ExecutedQuery>
) : ActionOnElementInterface, TestActionInterface {

    override val action: TestAction = TestAction(actionType())

    companion object {
        fun actionType() = "SpacialSearch"

        fun createSpacialSearchAction(direction : Direction,
                                      elementType : ElementType,
                                      digId: Int,
                                      prevQueries: List<ExecutedQuery>) : SpacialSearchAction {
            return SpacialSearchAction(direction, elementType, digId, prevQueries)
        }
    }
}
