package io.visualdig.element

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.damo.aspen.Test
import io.visualdig.DigController
import io.visualdig.spacial.DigSpacialSearch
import io.visualdig.spacial.Direction
import io.visualdig.spacial.SearchPriority
import org.assertj.core.api.Assertions.assertThat
import java.util.Arrays.asList

class DigWebElementTest : Test({

    describe("#click") {
        test("happy path") {
            val controller: DigController = mock()
            val prevQueries = asList<DigElementQuery>(DigTextQuery(text = "foo"))
            val elem = DigWebElement(digId = 1,
                    htmlId = null,
                    prevQueries = prevQueries,
                    controller = controller)


            elem.click()


            verify(controller).click(1, prevQueries)
        }
    }

    describe("#spacialSearch") {
        test("happy path") {
            val controller: DigController = mock()
            val elem = DigWebElement(digId = 1,
                    htmlId = null,
                    prevQueries = asList<DigElementQuery>(),
                    controller = controller)


            val actualResult = elem.spacialSearch(Direction.EAST)


            val expectedSpacialSearchObj = DigSpacialSearch(
                    controller,
                    Direction.EAST,
                    elem,
                    emptyList<DigElementQuery>(),
                    toleranceInPixels = 20,
                    searchPriority = SearchPriority.ALIGNMENT_THEN_DISTANCE)
            assertThat(actualResult).isEqualTo(expectedSpacialSearchObj)
        }

        test("override tolerance") {
            val controller: DigController = mock()
            val elem = DigWebElement(digId = 1,
                    htmlId = null,
                    prevQueries = asList<DigElementQuery>(),
                    controller = controller)

            val actualResult = elem.spacialSearch(
                    direction = Direction.EAST,
                    toleranceInPixels = 21)


            val expectedSpacialSearchObj = DigSpacialSearch(
                    controller = controller,
                    direction = Direction.EAST,
                    element = elem,
                    prevQueries = emptyList<DigElementQuery>(),
                    toleranceInPixels = 21,
                    searchPriority = SearchPriority.ALIGNMENT_THEN_DISTANCE)
            assertThat(actualResult).isEqualTo(expectedSpacialSearchObj)
        }

        test("override search priority") {
            val controller: DigController = mock()
            val elem = DigWebElement(digId = 1,
                    htmlId = null,
                    prevQueries = asList<DigElementQuery>(),
                    controller = controller)

            val actualResult = elem.spacialSearch(
                    direction = Direction.EAST,
                    searchPriority = SearchPriority.DISTANCE)


            val expectedSpacialSearchObj = DigSpacialSearch(
                    controller = controller,
                    direction = Direction.EAST,
                    element = elem,
                    prevQueries = emptyList<DigElementQuery>(),
                    toleranceInPixels = 20,
                    searchPriority = SearchPriority.DISTANCE)

            assertThat(actualResult).isEqualTo(expectedSpacialSearchObj)
        }
    }
})