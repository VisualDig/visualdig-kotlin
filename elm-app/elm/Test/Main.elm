module Test.Main exposing (..)

import Test exposing (concat)
import Test.ActionTest
import Json.Encode exposing (Value)
import Test.Emit exposing (emit)
import Test.Runner.Node exposing (TestProgram, run)


main : TestProgram
main =
    [ Test.ActionTest.suite ]
        |> concat
        |> run emit
