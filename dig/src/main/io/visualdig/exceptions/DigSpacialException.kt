package io.visualdig.exceptions

import io.visualdig.actions.SpacialSearchAction
import io.visualdig.results.CloseResult
import io.visualdig.results.SpacialSearchResult
import io.visualdig.spacial.Direction
import java.lang.Math.abs
import java.util.*

class DigSpacialException(action: SpacialSearchAction, result: SpacialSearchResult)
    : Exception(getDetailedMessage(action, result)) {

    companion object {
        private fun getDetailedMessage(action : SpacialSearchAction, result : SpacialSearchResult) : String {
            val direction = action.direction.description.toLowerCase(Locale.ENGLISH)
            val elementType = action.elementType.description.toLowerCase(Locale.ENGLISH)
            val queryText = action.prevQueries.first().queryDescription()
            val errorText : String
            if(result.closeResults.isEmpty()) {
                errorText = """
Unable to find $elementType $direction of $queryText.

There are no close matches.

This is likely because the element isn't visible or it is actually east of $queryText.

"""
            } else {
                val htmlId = result.closeResults.first().htmlId
                val spacialDescription = getSpacialDescription(action.direction, result.closeResults.first())
                errorText = """
Unable to find $elementType $direction of $queryText.

The closest match was an element with id: $htmlId.

$spacialDescription
"""
            }
            return errorText
        }

        private fun getSpacialDescription(direction: Direction, result : CloseResult) : String {
            val directionText = direction.description.toLowerCase(Locale.ENGLISH)
            var offDirection = ""
            var offAmount = 0
            var isAlignmentMessage = true
            when(direction) {
                Direction.EAST  -> {
                    offAmount = (abs(result.y) - result.tolerance)
                    if(result.y < 0) {
                        offDirection = "south"
                    } else {
                        offDirection = "north"
                    }
                    isAlignmentMessage = result.x > 0
                }
                Direction.WEST -> {
                    offAmount = (abs(result.y) - result.tolerance)
                    if(result.y < 0) {
                        offDirection = "south"
                    } else {
                        offDirection = "north"
                    }
                    isAlignmentMessage = result.x < 0
                }
                Direction.NORTH -> {
                    offAmount = (abs(result.x) - result.tolerance)
                    if(result.x < 0) {
                        offDirection = "west"
                    } else {
                        offDirection = "east"
                    }
                    isAlignmentMessage = result.y > 0
                }
                Direction.SOUTH -> {
                    offAmount = (abs(result.x) - result.tolerance)
                    if(result.x < 0) {
                        offDirection = "west"
                    } else {
                        offDirection = "east"
                    }
                    isAlignmentMessage = result.y < 0
                }
            }

            if(isAlignmentMessage) {
                return "The element was $offAmount pixels too far $offDirection to be considered aligned $directionText."
            }

            return "TODO WOOPS"
        }
    }
}