package io.virtualdig.element

import io.virtualdig.actions.TestActionInterface
import java.lang.reflect.Type

interface DigElementQuery {
    fun action() : TestActionInterface
}

interface DigImplElementQuery<T : TestActionInterface> : DigElementQuery {
    fun specificAction() : T
}