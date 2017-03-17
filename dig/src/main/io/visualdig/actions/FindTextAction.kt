package io.visualdig.actions


data class FindTextAction(val text: String) : TestActionInterface {
    override val action: TestAction = TestAction(FindTextAction.actionType())

    companion object {
        fun actionType() = "FindText"
    }
}

