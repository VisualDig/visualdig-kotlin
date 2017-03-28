package io.visualdig.spacial

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import io.visualdig.DigController
import io.visualdig.actions.ExecutedQuery.Companion.createExecutedQuery
import io.visualdig.actions.SpacialSearchAction
import io.visualdig.element.DigElementQuery
import io.visualdig.element.DigSpacialQuery
import io.visualdig.element.DigTextQuery
import io.visualdig.element.DigWebElement
import io.visualdig.results.Result
import io.visualdig.results.SpacialSearchResult
import org.assertj.core.api.Assertions
import java.util.Arrays.asList

class DigSpacialSearchTest : Test({
    describe("#forCheckbox") {
        test("happy path") {
            val controllerMock: DigController = mock()
            val prevQueries: List<DigElementQuery> = asList(DigTextQuery("findtext"))
            val anchorElem = DigWebElement(1, null, prevQueries, controllerMock)
            val spacialSearch = DigSpacialSearch(controller = controllerMock,
                    direction = Direction.EAST,
                    element = anchorElem,
                    prevQueries = prevQueries,
                    toleranceInPixels = 10,
                    searchPriority = SearchPriority.ALIGNMENT)


            val spacialSearchResult = SpacialSearchResult(
                    result = Result.Success,
                    digId = 2,
                    htmlId = "foo-html-id",
                    closeResults = emptyList())

            whenever(controllerMock.search(any())).thenReturn(spacialSearchResult)


            val actualResult = spacialSearch.forCheckbox()


            val spacialQuery = DigSpacialQuery(
                    direction = Direction.EAST,
                    elementType = ElementType.CHECKBOX,
                    tolerance = 10,
                    priority = SearchPriority.ALIGNMENT,
                    digId = anchorElem.digId)
            val foundElem = DigWebElement(2, "foo-html-id", asList(spacialQuery).plus(prevQueries), controllerMock)
            Assertions.assertThat(actualResult).isEqualTo(foundElem)

            val spacialSearchAction = SpacialSearchAction(direction = Direction.EAST,
                    elementType = ElementType.CHECKBOX,
                    digId = spacialSearch.element.digId,
                    prevQueries = prevQueries.map(::createExecutedQuery),
                    toleranceInPixels = 10,
                    priority = spacialSearch.searchPriority
            )
            verify(controllerMock).search(spacialSearchAction)
        }
    }
})