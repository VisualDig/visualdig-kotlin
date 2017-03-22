module Test.ResultExtras exposing (..)

import Expect exposing (Expectation)


resultFailedContainingText : String -> Result String a -> Expectation
resultFailedContainingText text result =
    case result of
        Err msg ->
            if String.contains text msg then
                Expect.pass
            else
                Expect.fail ("Result failed as expected, but did not contain text: " ++ text ++ """

Error message: """ ++ msg)

        Ok _ ->
            Expect.fail "Result was ok when it should have failed"
