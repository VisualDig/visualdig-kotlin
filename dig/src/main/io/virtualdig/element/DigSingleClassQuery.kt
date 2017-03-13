package io.virtualdig.element

import io.virtualdig.actions.FindSingleClassAction
import io.virtualdig.actions.TestActionInterface

class DigSingleClassQuery(private val singleClass : String) : DigImplElementQuery<FindSingleClassAction>
{
    override fun action(): TestActionInterface {
        return specificAction()
    }

    override fun specificAction() : FindSingleClassAction {
        return FindSingleClassAction(singleClass = this.singleClass)
    }
}