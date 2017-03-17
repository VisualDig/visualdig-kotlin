package io.visualdig.element

import io.visualdig.actions.FindSingleClassAction
import io.visualdig.actions.TestActionInterface

data class DigSingleClassQuery(private val singleClass : String) : DigImplElementQuery<FindSingleClassAction>
{
    override fun action(): TestActionInterface {
        return specificAction()
    }

    override fun specificAction() : FindSingleClassAction {
        return FindSingleClassAction(singleClass = this.singleClass)
    }
}