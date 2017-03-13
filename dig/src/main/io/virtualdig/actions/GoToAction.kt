package io.virtualdig.actions

data class GoToAction(override val action: TestAction = TestAction("GoTo"),
                      val uri: String) : TestActionInterface
