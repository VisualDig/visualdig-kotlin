module Ports.Model exposing (..)

import Actions.Decoder exposing (SpacialSearchAction)
import Queries exposing (ExecutedQuery, SpacialQuery, TextQuery)


type alias SpacialQueryData =
    { direction : String
    , elementType : String
    , digId : Int
    }


type alias ExecutedQueryData =
    { queryType : String
    , textQuery : Maybe TextQuery
    , spacialQuery : Maybe SpacialQueryData
    }


type alias ClickPortData =
    { digId : Int
    , prevQueries : List ExecutedQueryData
    }


type alias SpacialSearchPortData =
    { direction : String
    , elementType : String
    , digId : Int
    , prevQueries : List ExecutedQueryData
    , tolerance : Int
    , priority : String
    }


spacialSearchPortData : SpacialSearchAction -> SpacialSearchPortData
spacialSearchPortData action =
    { direction = toString action.direction
    , elementType = toString action.elementType
    , digId = action.digId
    , prevQueries = List.map mapToExecutedQuery action.prevQueries
    , tolerance = action.tolerance
    , priority = toString action.priority
    }


clickPortData : Int -> List ExecutedQuery -> ClickPortData
clickPortData digId prevQueries =
    { digId = digId
    , prevQueries = List.map mapToExecutedQuery prevQueries
    }


mapToExecutedQuery : ExecutedQuery -> ExecutedQueryData
mapToExecutedQuery query =
    { queryType = toString query.queryType
    , textQuery = query.textQuery
    , spacialQuery = Maybe.map mapToSpacialQueryData query.spacialQuery
    }


mapToSpacialQueryData : SpacialQuery -> SpacialQueryData
mapToSpacialQueryData query =
    { direction = toString query.direction
    , elementType = toString query.elementType
    , digId = query.digId
    }
