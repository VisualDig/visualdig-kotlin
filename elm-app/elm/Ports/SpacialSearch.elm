port module Ports.SpacialSearch exposing (..)

import Ports.Model exposing (SpacialSearchPortData)
import Response exposing (SpacialSearchResult)


port spacialSearch : SpacialSearchPortData -> Cmd msg


port spacialSearch_result : (SpacialSearchResult -> msg) -> Sub msg
