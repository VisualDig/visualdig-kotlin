module Response exposing (..)

import Json.Encode exposing (Value, encode, int, list, object, string)
import Json.Encode.Extra exposing (maybe)


type alias TestResult =
    { result : ActionResult
    , message : String
    }


type alias TestResultJS =
    { result : String
    , message : String
    }


type alias FindTextSearchResult =
    { result : String
    , digId : Maybe Int
    , closestMatches : List String
    }


type alias SpacialSearchResult =
    { result : String
    , message : String
    , digId : Maybe Int
    , closeResults : List CloseSpacialResult
    }


type alias CloseSpacialResult =
    { x : Int
    , y : Int
    , tolerance : Int
    , htmlId : String
    }


type ActionResult
    = Success
    | Failure


actionResult : String -> ActionResult
actionResult value =
    case value of
        "Success" ->
            Success

        _ ->
            Failure


basicSuccessResult : TestResult
basicSuccessResult =
    { result = Success, message = "" }


encodeJsonResult : TestResult -> String
encodeJsonResult result =
    encode 4
        (object
            [ ( "result", string (toString result.result) )
            , ( "message", string result.message )
            ]
        )


encodeFindTextResult : FindTextSearchResult -> String
encodeFindTextResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "digId", maybe int result.digId )
            , ( "closestMatches", list (List.map (\a -> string a) result.closestMatches) )
            ]
        )


encodeSpacialSearchResult : SpacialSearchResult -> String
encodeSpacialSearchResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "message", string result.message )
            , ( "digId", maybe int result.digId )
            , ( "closeResults", list (List.map encodeCloseSpacialSearchResult result.closeResults) )
            ]
        )


encodeCloseSpacialSearchResult : CloseSpacialResult -> Value
encodeCloseSpacialSearchResult result =
    (object
        [ ( "x", int result.x )
        , ( "y", int result.y )
        , ( "tolerance", int result.tolerance )
        , ( "htmlId", string result.htmlId )
        ]
    )
