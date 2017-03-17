package io.visualdig.element

import io.visualdig.actions.FindTextAction
import io.visualdig.actions.TestActionInterface

data class DigTextQuery(val text : String) : DigImplElementQuery<FindTextAction>
{
    override fun action(): TestActionInterface {
        return specificAction()
    }

    override fun specificAction() : FindTextAction {
        return FindTextAction(text = this.text)
    }

    companion object {
        fun queryType() = "TextQuery"
    }
}