package io.virtualdig.element

import io.virtualdig.DigController

data class DigWebElement(val digId : Int,
                         val queryUsed: DigElementQuery,
                         private val controller: DigController) {

    fun click() {
        controller.click(digId, queryUsed)
    }
}