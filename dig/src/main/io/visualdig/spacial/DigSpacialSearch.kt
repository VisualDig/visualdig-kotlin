package io.visualdig.spacial

import io.visualdig.DigController
import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigWebElement
import io.visualdig.exceptions.DigFatalException
import java.util.Arrays.asList

data class DigSpacialSearch(private val controller: DigController,
                            val direction: Direction,
                            val element: DigWebElement,
                            private val prevQueries: List<DigElementQuery>,
                            val toleranceInPixels: Int,
                            val searchPriority: SearchPriority) {

    fun forCheckbox(): DigWebElement {
        val spacialSearchQuery = DigSpacialQuery(
                direction,
                ElementType.CHECKBOX,
                toleranceInPixels,
                searchPriority,
                element.digId)

        val spacialSearchResult = controller.search(spacialSearchQuery.specificAction(prevQueries))

        if (spacialSearchResult.digId == null) {
            throw DigFatalException("A more specific exception should have been thrown already. Could not find element using dig id.")
        } else {
            return DigWebElement(digId = spacialSearchResult.digId,
                    htmlId = spacialSearchResult.htmlId,
                    prevQueries = asList(spacialSearchQuery).plus(prevQueries),
                    controller = controller)
        }
    }
}