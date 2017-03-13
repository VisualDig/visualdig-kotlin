port module Ports.FindText exposing (..)

import Response exposing (ActionResult)


type alias FindTextSearchResult =
    { result : String
    , closestMatches : List String
    }


port findText_search : String -> Cmd msg


port findText_searchResult : (FindTextSearchResult -> msg) -> Sub msg
