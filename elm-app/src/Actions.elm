module Actions exposing (..)

import Json.Decode exposing (Decoder, field, map, string, succeed)
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


type ActionType
    = GoTo
    | FindText


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

        _ ->
            Nothing
