package io.visualdig.exceptions

import io.visualdig.actions.SpacialSearchAction
import io.visualdig.results.CloseResult
import io.visualdig.results.Result
import io.visualdig.results.SpacialSearchResult
import io.visualdig.spacial.Direction
import io.visualdig.spacial.SearchPriority
import java.lang.Math.abs
import java.util.*

class DigSpacialException(action: SpacialSearchAction, result: SpacialSearchResult)
    : Exception(getDetailedMessage(action, result)) {

    companion object {
        private fun getDetailedMessage(action: SpacialSearchAction, result: SpacialSearchResult): String {
            val direction = action.direction.description.toLowerCase(Locale.ENGLISH)
            val elementType = action.elementType.description.toLowerCase(Locale.ENGLISH)
            val queryText = action.prevQueries.first().queryDescription()
            val errorText: String
            when (result.result) {
                Result.Failure_NoMatch -> {
                    if (result.closeResults.isEmpty()) {
                        errorText = noCloseMatchesMessage(direction, elementType, queryText)
                    } else {
                        val htmlId = result.closeResults.first().htmlId
                        val spacialDescription = getSpacialDescription(action.direction, result.closeResults.first())
                        errorText = closeMatchMessage(direction, elementType, htmlId, queryText, spacialDescription)
                    }
                }
                Result.Failure_AmbiguousMatch -> {
                    if(action.priority == SearchPriority.DISTANCE) {
                        errorText = ambiguousDistanceMatchMessage(direction, elementType, result.closeResults, queryText)
                    } else if(action.priority == SearchPriority.ALIGNMENT) {
                        errorText = ambiguousAlignmentMatchMessage(direction, elementType, result.closeResults, queryText)
                    } else {
                        throw DigFatalException("Result should not be ambiguous if alignment and distance based")
                    }
                }
                Result.Failure_QueryExpired -> { errorText = "Query expired TODO"}
                Result.Success -> {
                    throw DigFatalException("Result from spacial search should not be successful inside of an exception method")
                }
            }
            return errorText
        }

        private fun ambiguousDistanceMatchMessage(direction: String, elementType: String, closeResults: List<CloseResult>, queryText: String): String {
            val foundString = "$elementType B and $elementType C which both are the same distance from element A."
            val capitalizeFoundString = foundString.capitalize()

            val bElementInfoText : String
            if (closeResults[0].htmlId == null) {
                bElementInfoText = "First ambiguous element (no HTML id)"
            } else {
                val htmlId = closeResults[0].htmlId
                bElementInfoText = "First ambiguous element with HTML id: $htmlId"
            }

            val cElementInfoText : String
            if (closeResults[1].htmlId == null) {
                cElementInfoText = "Second ambiguous element (no HTML id)"
            } else {
                val htmlId = closeResults[1].htmlId
                cElementInfoText = "Second ambiguous element with HTML id: $htmlId"
            }

            return """
Could not find an unambiguous $elementType element $direction of element A.

Expected:

     ___      ___
    |   |    |   |
    | A |    | B |
    |___|    |___|


Found:

    $capitalizeFoundString

Suggestions:

    - Are these elements overlapping on the web page?
    - Try setting the priority to DistanceThenAlignment or AlignmentThenDistance
      to resolve which factor matters more in this test: distance or alignment.

Additional Info:

    A = $queryText
    B = $bElementInfoText
    C = $cElementInfoText
"""
        }

        private fun ambiguousAlignmentMatchMessage(direction: String, elementType: String, closeResults: List<CloseResult>, queryText: String): String {
            val foundString = "$elementType B and $elementType C which both have the same alignment relative to element A."
            val capitalizeFoundString = foundString.capitalize()

            val bElementInfoText : String
            if (closeResults[0].htmlId == null) {
                bElementInfoText = "First ambiguous element (no HTML id)"
            } else {
                val htmlId = closeResults[0].htmlId
                bElementInfoText = "First ambiguous element with HTML id: $htmlId"
            }

            val cElementInfoText : String
            if (closeResults[1].htmlId == null) {
                cElementInfoText = "Second ambiguous element (no HTML id)"
            } else {
                val htmlId = closeResults[1].htmlId
                cElementInfoText = "Second ambiguous element with HTML id: $htmlId"
            }

            return """
Could not find an unambiguous $elementType element $direction of element A.

Expected:

     ___      ___
    |   |    |   |
    | A |    | B |
    |___|    |___|


Found:

    $capitalizeFoundString

Suggestions:

    - Are these elements overlapping on the web page?
    - Try setting the priority to DistanceThenAlignment or AlignmentThenDistance
      to resolve which factor matters more in this test: distance or alignment.

Additional Info:

    A = $queryText
    B = $bElementInfoText
    C = $cElementInfoText
"""
        }

        private fun closeMatchMessage(direction: String, elementType: String, htmlId: String?, queryText: String, spacialDescription: String): String {
            val closestMatchIdText : String
            if(htmlId == null) {
                closestMatchIdText = "There was a close match, but it has no HTML id."
            } else {
                closestMatchIdText = "The closest match was an element with id: $htmlId."
            }

            return """
Unable to find $elementType $direction of $queryText.

$closestMatchIdText

$spacialDescription
"""
        }

        private fun noCloseMatchesMessage(direction: String, elementType: String, queryText: String): String {
            return """
Unable to find $elementType $direction of $queryText.

There are no close matches.

This is likely because the element isn't visible or it is actually east of $queryText.

"""
        }

        private fun getSpacialDescription(direction: Direction, result: CloseResult): String {
            val directionText = direction.description.toLowerCase(Locale.ENGLISH)
            var offDirection = ""
            var offAmount = 0
            var isAlignmentMessage = true
            when (direction) {
                Direction.EAST -> {
                    offAmount = (abs(result.y) - result.tolerance)
                    if (result.y < 0) {
                        offDirection = "south"
                    } else {
                        offDirection = "north"
                    }
                    isAlignmentMessage = result.x > 0
                }
                Direction.WEST -> {
                    offAmount = (abs(result.y) - result.tolerance)
                    if (result.y < 0) {
                        offDirection = "south"
                    } else {
                        offDirection = "north"
                    }
                    isAlignmentMessage = result.x < 0
                }
                Direction.NORTH -> {
                    offAmount = (abs(result.x) - result.tolerance)
                    if (result.x < 0) {
                        offDirection = "west"
                    } else {
                        offDirection = "east"
                    }
                    isAlignmentMessage = result.y > 0
                }
                Direction.SOUTH -> {
                    offAmount = (abs(result.x) - result.tolerance)
                    if (result.x < 0) {
                        offDirection = "west"
                    } else {
                        offDirection = "east"
                    }
                    isAlignmentMessage = result.y < 0
                }
            }

            if (isAlignmentMessage) {
                return "The element was $offAmount pixels too far $offDirection to be considered aligned $directionText."
            }

            return "TODO WOOPS"
        }
    }
}