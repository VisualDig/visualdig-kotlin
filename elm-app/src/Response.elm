module Response
    exposing
        ( TestResult
        , ActionResult(..)
        , actionResult
        , basicSuccessResult
        , encodeJsonResult
        , encodeFindTextResult
        )

import Json.Encode exposing (Value, encode, list, object, string)


type alias TestResult =
    { result : ActionResult
    , message : String
    }


type alias FindTextResult =
    { result : ActionResult
    , closestMatches : List String
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


encodeFindTextResult : FindTextResult -> String
encodeFindTextResult result =
    encode 4
        (object
            [ ( "result", string (toString result.result) )
            , ( "closestMatches", list (List.map (\a -> string a) result.closestMatches) )
            ]
        )
