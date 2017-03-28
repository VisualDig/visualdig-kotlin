package io.visualdig.actions

import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType
import io.visualdig.spacial.SearchPriority

data class SpacialSearchAction(
        val direction : Direction,
        val elementType : ElementType,
        override val digId: Int,
        override val prevQueries: List<ExecutedQuery>,
        val toleranceInPixels : Int,
        val priority : SearchPriority) : ActionOnElementInterface, TestActionInterface {

    override val action: TestAction = TestAction(actionType())

    companion object {
        fun actionType() = "SpacialSearch"

        fun createSpacialSearchAction(direction : Direction,
                                      elementType : ElementType,
                                      digId: Int,
                                      prevQueries: List<ExecutedQuery>,
                                      toleranceInPixels: Int = 20,
                                      priority: SearchPriority  = SearchPriority.ALIGNMENT_THEN_DISTANCE): SpacialSearchAction {
            return SpacialSearchAction(direction, elementType, digId, prevQueries, toleranceInPixels, priority)
        }
    }
}
