package io.visualdig.spacial

import io.visualdig.DigController
import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigWebElement
import io.visualdig.exceptions.DigFatalException

class DigSpacialSearch(private val controller : DigController,
                       val direction : Direction,
                       val element : DigWebElement) {

    fun forCheckbox() : DigWebElement {
        val spacialSearchQuery = DigSpacialQuery(direction, ElementType.CHECKBOX, element.digId)
        val spacialSearchResult = controller.search(spacialSearchQuery.specificAction(element.prevQueries))

        if(spacialSearchResult.digId == null) {
            throw DigFatalException("A more specific exception should have been thrown already. Could not find element using dig id.")
        } else {
            val newPrevList : MutableList<DigElementQuery> = mutableListOf(spacialSearchQuery)
            newPrevList.addAll(element.prevQueries)

            return DigWebElement(digId = spacialSearchResult.digId,
                                 prevQueries = newPrevList.toList(),
                                 controller = controller)
        }
    }
}