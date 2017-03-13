package io.virtualdig.actions

data class TestAction (
    val actionType : String
)

interface TestActionInterface {
    val action : TestAction
}