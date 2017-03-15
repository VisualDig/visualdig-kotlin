package io.virtualdig.actions

data class FindSingleClassAction(val singleClass: String) : TestActionInterface {
    override val action: TestAction = TestAction(FindSingleClassAction.actionType())

    companion object {
        fun actionType() = "FindSingleClass"
    }
}