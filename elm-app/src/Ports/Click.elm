port module Ports.Click exposing (..)

import Actions exposing (ClickAction)
import Response exposing (TestResultJS)


port click_searchText : { digId : Int, textQuery : String } -> Cmd msg


port click_searchResult : (TestResultJS -> msg) -> Sub msg
