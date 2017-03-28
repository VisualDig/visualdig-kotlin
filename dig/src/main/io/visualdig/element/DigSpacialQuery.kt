package io.visualdig.element

import io.visualdig.actions.ExecutedQuery.Companion.createExecutedQuery
import io.visualdig.actions.SpacialSearchAction
import io.visualdig.actions.TestActionInterface
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType
import io.visualdig.spacial.SearchPriority

data class DigSpacialQuery(val direction: Direction,
                           val elementType: ElementType,
                           val tolerance: Int,
                           val priority: SearchPriority,
                           override val digId: Int) : DigChainElementQuery<SpacialSearchAction> {
    override fun action(): TestActionInterface {
        return specificAction(emptyList())
    }

    override fun specificAction(prevQueries: List<DigElementQuery>): SpacialSearchAction {
        return SpacialSearchAction.createSpacialSearchAction(
                direction = direction,
                elementType = elementType,
                digId = digId,
                prevQueries = prevQueries.map(::createExecutedQuery),
                toleranceInPixels = tolerance,
                priority = priority)
    }

    companion object {
        fun queryType() = "SpacialQuery"
    }
}