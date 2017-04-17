module Actions.Decoder exposing (..)

import Actions.FindText exposing (FindTextAction)
import Json.Decode exposing (Decoder, andThen, at, fail, field, int, list, map, maybe, string, succeed)
import Json.Decode.Extra exposing ((|:))
import Queries exposing (Direction, ElementType, ExecutedQuery, SearchPriority, SpacialQuery, TextQuery, directionDecoder, elementTypeDecoder, executedQueryDecoder, searchPriorityDecoder, spacialQueryDecoder, textQueryDecoder)


type alias GoToAction =
    { uri : String
    }


type alias ClickAction =
    { digId : Int
    , prevQueries : List ExecutedQuery
    }


type alias SpacialSearchAction =
    { direction : Direction
    , elementType : ElementType
    , tolerance : Int
    , priority : SearchPriority
    , digId : Int
    , prevQueries : List ExecutedQuery
    }


type Action
    = GoTo GoToAction
    | FindText FindTextAction
    | Click ClickAction
    | SpacialSearch SpacialSearchAction


clickActionDecoder : Decoder ClickAction
clickActionDecoder =
    succeed ClickAction
        |: (field "digId" int)
        |: (field "prevQueries" (list executedQueryDecoder))


spacialSearchActionDecoder : Decoder SpacialSearchAction
spacialSearchActionDecoder =
    succeed SpacialSearchAction
        |: (field "direction" directionDecoder)
        |: (field "elementType" elementTypeDecoder)
        |: (field "toleranceInPixels" int)
        |: (field "priority" searchPriorityDecoder)
        |: (field "digId" int)
        |: (field "prevQueries" (list executedQueryDecoder))


findTextActionDecoder : Decoder FindTextAction
findTextActionDecoder =
    succeed FindTextAction
        |: (field "text" string)


goToActionDecoder : Decoder GoToAction
goToActionDecoder =
    succeed GoToAction
        |: (field "uri" string)


actionDecoder : Decoder Action
actionDecoder =
    (at
        [ "action", "actionType" ]
        string
    )
        |> andThen actionDecoderHelper


actionDecoderHelper : String -> Decoder Action
actionDecoderHelper action =
    case String.toLower action of
        "goto" ->
            map GoTo goToActionDecoder

        "findtext" ->
            map FindText findTextActionDecoder

        "click" ->
            map Click clickActionDecoder

        "spacialsearch" ->
            map SpacialSearch spacialSearchActionDecoder

        _ ->
            fail <|
                "Trying to decode action, but value "
                    ++ action
                    ++ " is not supported."
