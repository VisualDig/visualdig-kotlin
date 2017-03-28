module Response exposing (..)

import Json.Encode exposing (Value, encode, int, list, object, string)
import Json.Encode.Extra exposing (maybe)


type alias TestResult =
    { result : String
    , message : String
    }


type alias FindTextSearchResult =
    { result : String
    , digId : Maybe Int
    , htmlId : Maybe String
    , closestMatches : List String
    }


type alias SpacialSearchResult =
    { result : String
    , digId : Maybe Int
    , htmlId : Maybe String
    , closeResults : List CloseSpacialResult
    }


type alias CloseSpacialResult =
    { x : Int
    , y : Int
    , tolerance : Int
    , htmlId : String
    }


basicSuccessResult : TestResult
basicSuccessResult =
    { result = "Success", message = "" }


encodeJsonResult : TestResult -> String
encodeJsonResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "message", string result.message )
            ]
        )


encodeFindTextResult : FindTextSearchResult -> String
encodeFindTextResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "digId", maybe int result.digId )
            , ( "htmlId", maybe string result.htmlId )
            , ( "closestMatches", list (List.map (\a -> string a) result.closestMatches) )
            ]
        )


encodeSpacialSearchResult : SpacialSearchResult -> String
encodeSpacialSearchResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "digId", maybe int result.digId )
            , ( "htmlId", maybe string result.htmlId )
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
