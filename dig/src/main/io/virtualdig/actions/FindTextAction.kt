package io.virtualdig.actions

data class FindTextAction(override val action: TestAction = TestAction("FindText"),
                          val text: String) : TestActionInterface