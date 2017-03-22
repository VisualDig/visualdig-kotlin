package io.visualdig.element

import io.visualdig.actions.TestActionInterface
import java.lang.reflect.Type

interface DigElementQuery {
    fun action() : TestActionInterface
}

interface DigChainElementQuery<T : TestActionInterface> : DigElementQuery {
    val digId : Int

    fun specificAction(prevQueries : List<DigElementQuery>) : T
}

interface DigGenesisElementQuery<T : TestActionInterface> : DigElementQuery {
    fun specificAction() : T
}