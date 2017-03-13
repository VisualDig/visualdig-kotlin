package io.virtualdig.element

import io.virtualdig.actions.FindTextAction
import io.virtualdig.actions.TestActionInterface

class DigTextQuery(val text : String) : DigImplElementQuery<FindTextAction>
{
    override fun action(): TestActionInterface {
        return specificAction()
    }

    override fun specificAction() : FindTextAction {
        return FindTextAction(text = this.text)
    }
}