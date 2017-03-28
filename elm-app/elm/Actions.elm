module Actions exposing (..)

import Json.Decode exposing (Decoder, andThen, at, fail, field, int, list, map, maybe, string, succeed)
import Json.Decode.Extra exposing ((|:))
import Queries exposing (Direction, ElementType, ExecutedQuery, SearchPriority, SpacialQuery, TextQuery, directionDecoder, elementTypeDecoder, executedQueryDecoder, searchPriorityDecoder, spacialQueryDecoder, textQueryDecoder)


type alias GoToAction =
    { action : ActionType
    , uri : String
    }


type alias FindTextAction =
    { action : ActionType
    , text : String
    }


type alias ClickAction =
    { action : ActionType
    , digId : Int
    , prevQueries : List ExecutedQuery
    }


type alias SpacialSearchAction =
    { action : ActionType
    , direction : Direction
    , elementType : ElementType
    , tolerance : Int
    , priority : SearchPriority
    , digId : Int
    , prevQueries : List ExecutedQuery
    }


type ActionType
    = GoTo
    | FindText
    | Click
    | SpacialSearch


clickActionDecoder : Decoder ClickAction
clickActionDecoder =
    succeed ClickAction
        |: actionTypeDecoder
        |: (field "digId" int)
        |: (field "prevQueries" (list executedQueryDecoder))


spacialSearchActionDecoder : Decoder SpacialSearchAction
spacialSearchActionDecoder =
    succeed SpacialSearchAction
        |: actionTypeDecoder
        |: (field "direction" directionDecoder)
        |: (field "elementType" elementTypeDecoder)
        |: (field "toleranceInPixels" int)
        |: (field "priority" searchPriorityDecoder)
        |: (field "digId" int)
        |: (field "prevQueries" (list executedQueryDecoder))


findTextActionDecoder : Decoder FindTextAction
findTextActionDecoder =
    succeed FindTextAction
        |: actionTypeDecoder
        |: (field "text" string)


goToActionDecoder : Decoder GoToAction
goToActionDecoder =
    succeed GoToAction
        |: actionTypeDecoder
        |: (field "uri" string)


actionTypeDecoder : Decoder ActionType
actionTypeDecoder =
    (at
        [ "action", "actionType" ]
        (string |> andThen actionTypeHelper)
    )


actionTypeHelper : String -> Decoder ActionType
actionTypeHelper action =
    case String.toLower action of
        "goto" ->
            succeed GoTo

        "findtext" ->
            succeed FindText

        "click" ->
            succeed Click

        "spacialsearch" ->
            succeed SpacialSearch

        _ ->
            fail <|
                "Trying to decode action, but value "
                    ++ action
                    ++ " is not supported."
