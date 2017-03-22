package io.visualdig.element

import io.visualdig.actions.ExecutedQuery.Companion.createExecutedQuery
import io.visualdig.actions.SpacialSearchAction
import io.visualdig.actions.TestActionInterface
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType

data class DigSpacialQuery(val direction : Direction,
                           val elementType : ElementType,
                           override val digId : Int) : DigChainElementQuery<SpacialSearchAction>
{
    override fun action(): TestActionInterface {
        return specificAction(emptyList())
    }

    override fun specificAction(prevQueries : List<DigElementQuery>) : SpacialSearchAction {
        return SpacialSearchAction.createSpacialSearchAction(direction,
                                                             elementType,
                                                             digId,
                                                             prevQueries.map(::createExecutedQuery))
}

    companion object {
        fun queryType() = "SpacialQuery"
    }
}