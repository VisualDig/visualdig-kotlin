package io.visualdig.element

import io.visualdig.DigController
import io.visualdig.spacial.DigSpacialSearch
import io.visualdig.spacial.Direction
import io.visualdig.spacial.SearchPriority

data class DigWebElement(val digId: Int,
                         val htmlId: String?,
                         private val prevQueries: List<DigElementQuery>,
                         private val controller: DigController) {

    fun click() {
        controller.click(digId, prevQueries)
    }

    fun spacialSearch(direction: Direction,
                      toleranceInPixels: Int = 20,
                      searchPriority: SearchPriority = SearchPriority.ALIGNMENT_THEN_DISTANCE)
            : DigSpacialSearch {
        return DigSpacialSearch(controller = controller,
                direction = direction,
                element = this,
                prevQueries = prevQueries,
                toleranceInPixels = toleranceInPixels,
                searchPriority = searchPriority)
    }
}