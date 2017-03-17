package io.visualdig.actions

data class GoToAction(val uri: String) : TestActionInterface {
    override val action: TestAction = TestAction(GoToAction.actionType())

    companion object {
        fun actionType() = "GoTo"
    }
}
