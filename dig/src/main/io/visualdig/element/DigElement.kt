package io.visualdig.element

import io.visualdig.DigController

data class DigWebElement(val digId : Int,
                         val queryUsed: DigElementQuery,
                         private val controller: DigController) {

    fun click() {
        controller.click(digId, queryUsed)
    }
}