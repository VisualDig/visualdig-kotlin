module Test.ActionTest exposing (..)

import Actions.Decoder exposing (Action(..), actionDecoder)
import Expect
import Json.Decode exposing (decodeString)
import Queries exposing (Direction(West), ElementType(Checkbox), SearchPriority(Alignment))
import Test exposing (Test, describe, test)
import Test.ResultExtras exposing (resultFailedContainingText)


suite : Test
suite =
    describe "Actions"
        [ describe "Action decoder happy path"
            [ test "goto" <|
                \() ->
                    let
                        json =
                            """{
                                   "action": {
                                       "actionType": "GoTo"
                                   },
                                   "uri": "http://example.com/"
                               }"""
                    in
                        Expect.equal
                            (Ok (GoTo { uri = "http://example.com/" }))
                            (decodeString actionDecoder json)
            , test "findtext" <|
                \() ->
                    let
                        json =
                            """{
                                   "action": {
                                       "actionType": "FindText"
                                   },
                                   "text": "fooText"
                               }"""
                    in
                        Expect.equal
                            (Ok (FindText { text = "fooText" }))
                            (decodeString actionDecoder json)
            , test "click" <|
                \() ->
                    let
                        json =
                            """{
                                    "action": {
                                        "actionType": "Click"
                                    },
                                    "digId": 32,
                                    "prevQueries": []
                                }"""
                    in
                        Expect.equal
                            (Ok (Click { digId = 32, prevQueries = [] }))
                            (decodeString actionDecoder json)
            , test "spacialsearch" <|
                \() ->
                    let
                        json =
                            """{
                                    "action": {
                                        "actionType": "SpacialSearch"
                                    },
                                    "digId": 13,
                                    "direction": "WEST",
                                    "elementType": "CHECKBOX",
                                    "toleranceInPixels": 10,
                                    "priority": "ALIGNMENT",
                                    "prevQueries": []
                              }"""

                        expectedRecord =
                            { digId = 13
                            , direction = West
                            , elementType = Checkbox
                            , tolerance = 10
                            , priority = Alignment
                            , prevQueries = []
                            }
                    in
                        Expect.equal
                            (Ok (SpacialSearch expectedRecord))
                            (decodeString actionDecoder json)
            , test "unknown action type" <|
                \() ->
                    let
                        json =
                            """{
                                     "action": {
                                         "actionType": "GoGoDancer"
                                     }
                                 }"""
                    in
                        resultFailedContainingText
                            "Trying to decode action"
                            (decodeString actionDecoder json)
            ]
        ]
