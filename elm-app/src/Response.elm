module Response exposing (TestResult, successResult, encodeJsonResult)

import Json.Encode exposing (Value, encode, object, string)


type alias TestResult =
    { result : String
    , message : String
    }


successResult : TestResult
successResult =
    { result = "Success", message = "" }


encodeJsonResult : TestResult -> String
encodeJsonResult result =
    encode 4
        (object
            [ ( "result", string result.result )
            , ( "message", string result.message )
            ]
        )
