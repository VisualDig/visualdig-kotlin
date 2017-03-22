module Queries exposing (..)

import Json.Decode exposing (Decoder, andThen, fail, field, int, maybe, null, string, succeed)
import Json.Decode.Extra exposing ((|:))


type Direction
    = East
    | West
    | South
    | North


type ElementType
    = Checkbox


type QueryType
    = Text
    | SpacialSearch


type alias ExecutedQuery =
    { queryType : QueryType
    , textQuery : Maybe TextQuery
    , spacialQuery : Maybe SpacialQuery
    }


type alias TextQuery =
    { text : String }


type alias SpacialQuery =
    { direction : Direction
    , elementType : ElementType
    , digId : Int
    }


executedQueryDecoder : Decoder ExecutedQuery
executedQueryDecoder =
    succeed ExecutedQuery
        |: (field "queryType" queryTypeDecoder)
        |: (field "textQuery" (maybe textQueryDecoder))
        |: (field "spacialQuery" (maybe spacialQueryDecoder))


textQueryDecoder : Decoder TextQuery
textQueryDecoder =
    succeed TextQuery
        |: (field "text" string)


spacialQueryDecoder : Decoder SpacialQuery
spacialQueryDecoder =
    succeed SpacialQuery
        |: (field "direction" directionDecoder)
        |: (field "elementType" elementTypeDecoder)
        |: (field "digId" int)


queryTypeDecoder : Decoder QueryType
queryTypeDecoder =
    string |> andThen queryTypeHelper


queryTypeHelper : String -> Decoder QueryType
queryTypeHelper queryType =
    case String.toLower queryType of
        "textquery" ->
            succeed Text

        "spacialquery" ->
            succeed SpacialSearch

        _ ->
            fail <|
                "Trying to decode queryType, but value "
                    ++ queryType
                    ++ " is not supported."


directionDecoder : Decoder Direction
directionDecoder =
    string |> andThen directionHelper


directionHelper : String -> Decoder Direction
directionHelper direction =
    case String.toLower direction of
        "west" ->
            succeed West

        "east" ->
            succeed East

        "north" ->
            succeed North

        "south" ->
            succeed South

        _ ->
            fail <|
                "Trying to decode direction, but value "
                    ++ direction
                    ++ " is not supported."


elementTypeDecoder : Decoder ElementType
elementTypeDecoder =
    string |> andThen elementTypeHelper


elementTypeHelper : String -> Decoder ElementType
elementTypeHelper elementType =
    case String.toLower elementType of
        "checkbox" ->
            succeed Checkbox

        _ ->
            fail <|
                "Trying to decode elementType, but value "
                    ++ elementType
                    ++ " is not supported."
