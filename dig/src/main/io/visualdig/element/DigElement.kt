package io.visualdig.element

import io.visualdig.DigController
import io.visualdig.spacial.DigSpacialSearch
import io.visualdig.spacial.Direction

data class DigWebElement(val digId : Int,
                         val prevQueries: List<DigElementQuery>,
                         private val controller: DigController) {

    fun click() {
        controller.click(digId, prevQueries)
    }

    fun spacialSearch(direction: Direction) : DigSpacialSearch {
        return DigSpacialSearch(controller, direction, this)
    }
}