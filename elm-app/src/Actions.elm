module Actions exposing (..)

import Json.Decode exposing (Decoder, field, int, map, maybe, string, succeed)
import Json.Decode.Extra exposing ((|:))


type alias ActionCommon =
    { actionType : String }


type alias Action =
    { action : ActionCommon }


type alias GoToAction =
    { action : ActionCommon
    , uri : String
    }


type alias FindTextAction =
    { action : ActionCommon
    , text : String
    }


type alias TextQuery =
    { text : String }


type alias ClickAction =
    { action : ActionCommon
    , digId : Int
    , usedQueryType : String
    , usedTextQuery : Maybe TextQuery
    }


type ActionType
    = GoTo
    | FindText
    | Click


clickActionDecoder : Decoder ClickAction
clickActionDecoder =
    succeed ClickAction
        |: (field "action" actionDecoder)
        |: (field "digId" int)
        |: (field "usedQueryType" string)
        |: (field "usedTextQuery" (maybe textQueryDecoder))


findTextActionDecoder : Decoder FindTextAction
findTextActionDecoder =
    succeed FindTextAction
        |: (field "action" actionDecoder)
        |: (field "text" string)


goToActionDecoder : Decoder GoToAction
goToActionDecoder =
    succeed GoToAction
        |: (field "action" actionDecoder)
        |: (field "uri" string)


actionTypeDecoder : Decoder (Maybe ActionType)
actionTypeDecoder =
    map actionType
        (succeed Action
            |: (field "action" actionDecoder)
        )


textQueryDecoder : Decoder TextQuery
textQueryDecoder =
    succeed TextQuery
        |: (field "text" string)


actionDecoder : Decoder ActionCommon
actionDecoder =
    succeed ActionCommon
        |: (field "actionType" string)


actionType : Action -> Maybe ActionType
actionType action =
    case action.action.actionType of
        "GoTo" ->
            Just GoTo

        "FindText" ->
            Just FindText

        "Click" ->
            Just Click

        _ ->
            Nothing
