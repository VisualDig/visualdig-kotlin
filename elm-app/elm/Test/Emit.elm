port module Test.Emit exposing (..)

import Json.Encode exposing (Value)


port emit : ( String, Value ) -> Cmd msg
