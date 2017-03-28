port module Ports.FindText exposing (..)

import Response exposing (FindTextSearchResult)


port findText_search : String -> Cmd msg


port findText_searchResult : (FindTextSearchResult -> msg) -> Sub msg
