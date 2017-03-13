package io.virtualdig.actions

data class FindSingleClassAction(override val action: TestAction = TestAction("FindSingleClass"),
                                 val singleClass: String) : TestActionInterface