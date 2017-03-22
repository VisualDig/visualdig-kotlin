port module Ports.Click exposing (..)

import Actions exposing (ClickAction)
import Ports.Model exposing (ClickPortData)
import Response exposing (TestResultJS)


port click_searchText : ClickPortData -> Cmd msg


port click_searchResult : (TestResultJS -> msg) -> Sub msg
