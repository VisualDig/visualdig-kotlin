package io.visualdig.exceptions

import io.damo.aspen.Test
import io.visualdig.actions.ExecutedQuery
import io.visualdig.actions.SpacialSearchAction
import io.visualdig.element.DigTextQuery
import io.visualdig.results.CloseResult
import io.visualdig.results.Result
import io.visualdig.results.SpacialSearchResult
import io.visualdig.spacial.Direction
import io.visualdig.spacial.ElementType
import io.visualdig.spacial.SearchPriority
import org.assertj.core.api.Assertions
import java.util.*
import java.util.Arrays.asList

class DigSpacialExceptionTest : Test({
    val prevQueries = Arrays.asList(ExecutedQuery(queryType = DigTextQuery.queryType(),
            textQuery = DigTextQuery("foo text"),
            spacialQuery = null))

    describe("alignment") {
        test("east spacial query") {
            val action = SpacialSearchAction(
                    direction = Direction.EAST,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE_THEN_ALIGNMENT)

            val closestResult = CloseResult(x = 100, y = 21, tolerance = 20, htmlId = "id-of-elem")

            val result = SpacialSearchResult(result = Result.Failure_NoMatch,
                    digId = null,
                    closeResults = Arrays.asList(closestResult), htmlId = "foo-html-id")
            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Unable to find checkbox east of 'foo text' element.")
                    .withMessageContaining("The closest match was an element with id: id-of-elem.")
                    .withMessageContaining("The element was 1 pixels too far north to be considered aligned east.")
        }
        test("west spacial query") {
            val action = SpacialSearchAction(
                    direction = Direction.WEST,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE_THEN_ALIGNMENT)

            val closestResult = CloseResult(x = -100, y = -21, tolerance = 20, htmlId = "id-of-elem")

            val result = SpacialSearchResult(result = Result.Failure_NoMatch,
                    digId = null,
                    closeResults = Arrays.asList(closestResult), htmlId = "foo-html-id")
            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Unable to find checkbox west of 'foo text' element.")
                    .withMessageContaining("The closest match was an element with id: id-of-elem.")
                    .withMessageContaining("The element was 1 pixels too far south to be considered aligned west.")
        }
        test("north spacial query") {
            val action = SpacialSearchAction(
                    direction = Direction.NORTH,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE_THEN_ALIGNMENT)

            val closestResult = CloseResult(x = 50, y = 100, tolerance = 20, htmlId = "id-of-elem")

            val result = SpacialSearchResult(result = Result.Failure_NoMatch,
                    digId = null,
                    closeResults = Arrays.asList(closestResult), htmlId = "foo-html-id")
            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Unable to find checkbox north of 'foo text' element.")
                    .withMessageContaining("The closest match was an element with id: id-of-elem.")
                    .withMessageContaining("The element was 30 pixels too far east to be considered aligned north.")
        }
        test("south spacial query") {
            val action = SpacialSearchAction(
                    direction = Direction.SOUTH,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE_THEN_ALIGNMENT)

            val closestResult = CloseResult(x = 50, y = -100, tolerance = 20, htmlId = "id-of-elem")

            val result = SpacialSearchResult(result = Result.Failure_NoMatch,
                    digId = null,
                    closeResults = Arrays.asList(closestResult), htmlId = "foo-html-id")
            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Unable to find checkbox south of 'foo text' element.")
                    .withMessageContaining("The closest match was an element with id: id-of-elem.")
                    .withMessageContaining("The element was 30 pixels too far east to be considered aligned south.")
        }
        test("no close results") {
            val action = SpacialSearchAction(
                    direction = Direction.WEST,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE_THEN_ALIGNMENT)

            val result = SpacialSearchResult(result = Result.Failure_NoMatch,
                    digId = null,
                    closeResults = emptyList(), htmlId = "foo-html-id")

            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Unable to find checkbox west of 'foo text' element.")
                    .withMessageContaining("There are no close matches.")
                    .withMessageContaining("This is likely because the element isn't visible or it is actually east of 'foo text' element.")
        }
        test("ambiguous match for distance") {
            val closestResult1 = CloseResult(x = 50, y = -10, tolerance = 20, htmlId = "id-of-elem-1")
            val closestResult2 = CloseResult(x = 50, y = 10, tolerance = 20, htmlId = "id-of-elem-2")

            val action = SpacialSearchAction(
                    direction = Direction.EAST,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.DISTANCE)

            val result = SpacialSearchResult(result = Result.Failure_AmbiguousMatch,
                    digId = null,
                    closeResults = asList(closestResult1, closestResult2), htmlId = null)

            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Could not find an unambiguous checkbox element east of element A.")
                    .withMessageContaining("Expected:")
                    .withMessageContaining("Found:")
                    .withMessageContaining("    Checkbox B and checkbox C which both are the same distance from element A.")
                    .withMessageContaining("Suggestions:")
                    .withMessageContaining("    - Are these elements overlapping on the web page?")
                    .withMessageContaining("    - Try setting the priority to DistanceThenAlignment or AlignmentThenDistance")
                    .withMessageContaining("to resolve which factor matters more in this test: distance or alignment.")
                    .withMessageContaining("Additional Info:")
                    .withMessageContaining("    A = 'foo text' element")
                    .withMessageContaining("    B = First ambiguous element with HTML id: id-of-elem-1")
                    .withMessageContaining("    C = Second ambiguous element with HTML id: id-of-elem-2")
        }

        test("ambiguous match for alignment") {
            val closestResult1 = CloseResult(x = 50, y = 10, tolerance = 20, htmlId = "id-of-elem-1")
            val closestResult2 = CloseResult(x = 80, y = 10, tolerance = 20, htmlId = "id-of-elem-2")

            val action = SpacialSearchAction(
                    direction = Direction.EAST,
                    elementType = ElementType.CHECKBOX,
                    digId = 7,
                    prevQueries = prevQueries,
                    toleranceInPixels = 20,
                    priority = SearchPriority.ALIGNMENT)

            val result = SpacialSearchResult(result = Result.Failure_AmbiguousMatch,
                    digId = null,
                    closeResults = asList(closestResult1, closestResult2), htmlId = null)

            Assertions.assertThatExceptionOfType(DigSpacialException::class.java)
                    .isThrownBy { throw DigSpacialException(action, result) }
                    .withMessageContaining("Could not find an unambiguous checkbox element east of element A.")
                    .withMessageContaining("Expected:")
                    .withMessageContaining("Found:")
                    .withMessageContaining("    Checkbox B and checkbox C which both have the same alignment relative to element A.")
                    .withMessageContaining("Suggestions:")
                    .withMessageContaining("    - Are these elements overlapping on the web page?")
                    .withMessageContaining("    - Try setting the priority to DistanceThenAlignment or AlignmentThenDistance")
                    .withMessageContaining("to resolve which factor matters more in this test: distance or alignment.")
                    .withMessageContaining("Additional Info:")
                    .withMessageContaining("    A = 'foo text' element")
                    .withMessageContaining("    B = First ambiguous element with HTML id: id-of-elem-1")
                    .withMessageContaining("    C = Second ambiguous element with HTML id: id-of-elem-2")
        }
    }

})