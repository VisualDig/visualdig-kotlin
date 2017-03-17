package io.visualdig.element

import io.visualdig.actions.TestActionInterface
import java.lang.reflect.Type

interface DigElementQuery {
    fun action() : TestActionInterface
}

interface DigImplElementQuery<T : TestActionInterface> : DigElementQuery {
    fun specificAction() : T
}