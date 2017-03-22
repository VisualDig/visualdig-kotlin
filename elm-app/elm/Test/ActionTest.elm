module Test.ActionTest exposing (..)

import Actions exposing (ActionType(..), actionTypeDecoder)
import Expect
import Json.Decode exposing (decodeString)
import Test exposing (Test, describe, test)
import Test.ResultExtras exposing (resultFailedContainingText)


suite : Test
suite =
    describe "Actions"
        [ describe "ActionType decoder"
            [ test "goto" <|
                \() ->
                    let
                        json =
                            """{
                                   "action": {
                                       "actionType": "GoTo"
                                   }
                               }"""
                    in
                        Expect.equal
                            (Ok GoTo)
                            (decodeString actionTypeDecoder json)
            , test "findtext" <|
                \() ->
                    let
                        json =
                            """{
                                   "action": {
                                       "actionType": "FindText"
                                   }
                               }"""
                    in
                        Expect.equal
                            (Ok FindText)
                            (decodeString actionTypeDecoder json)
            , test "click" <|
                \() ->
                    let
                        json =
                            """{
                                    "action": {
                                        "actionType": "Click"
                                    }
                                }"""
                    in
                        Expect.equal
                            (Ok Click)
                            (decodeString actionTypeDecoder json)
            , test "spacialsearch" <|
                \() ->
                    let
                        json =
                            """{
                                  "action": {
                                      "actionType": "SpacialSearch"
                                  }
                              }"""
                    in
                        Expect.equal
                            (Ok SpacialSearch)
                            (decodeString actionTypeDecoder json)
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
                            (decodeString actionTypeDecoder json)
            ]
        ]
