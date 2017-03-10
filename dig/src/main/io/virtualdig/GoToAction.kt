package io.virtualdig

data class GoToAction(val action: TestAction = TestAction("GoTo"), val uri: String)
