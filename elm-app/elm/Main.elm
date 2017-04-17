module Main exposing (..)

import Actions.FindText exposing (..)
import Actions.Decoder exposing (Action(Click, FindText, GoTo, SpacialSearch), ClickAction, GoToAction, SpacialSearchAction, actionDecoder, clickActionDecoder, findTextActionDecoder, goToActionDecoder, spacialSearchActionDecoder)
import Ports.Click exposing (click_searchResult, click_searchText)
import Ports.FindText exposing (findText_search, findText_searchResult)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Json.Decode as Json
import Ports.Model exposing (clickPortData, spacialSearchPortData)
import Ports.SpacialSearch exposing (spacialSearch, spacialSearch_result)
import Queries exposing (QueryType(Text))
import Response exposing (FindTextSearchResult, SpacialSearchResult, TestResult, basicSuccessResult, encodeFindTextResult, encodeJsonResult, encodeSpacialSearchResult)
import Time exposing (Time, inSeconds, millisecond, second)
import WebSocket


main =
    Html.program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


digServer : String
digServer =
    "ws://localhost:8650/dig"


timeoutInSeconds : Float
timeoutInSeconds =
    4.5



-- MODEL


type alias Model =
    { websiteUrl : String
    , currentAction : Maybe Action
    , timeoutTime : Maybe Time
    , timeout : Bool
    }


clearCurrentAction : Model -> Model
clearCurrentAction existingModel =
    { existingModel
        | websiteUrl = existingModel.websiteUrl
        , currentAction = Nothing
        , timeoutTime = Nothing
        , timeout = False
    }


init : ( Model, Cmd Msg )
init =
    ( { websiteUrl = ""
      , currentAction = Nothing
      , timeoutTime = Nothing
      , timeout = False
      }
    , Cmd.none
    )



-- UPDATE


type Msg
    = NewMessage String
    | WebsiteLoaded
    | UpdateTick Time
    | FindTextUpdate FindTextSearchResult
    | ClickSearchUpdate TestResult
    | SpacialSearchUpdate SpacialSearchResult


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NewMessage str ->
            case (Json.decodeString actionDecoder str) of
                Ok (GoTo action) ->
                    ( { model
                        | websiteUrl = action.uri
                        , currentAction = Just (GoTo action)
                      }
                    , Cmd.none
                    )

                Ok (FindText action) ->
                    ( { model
                        | currentAction = Just (FindText action)
                      }
                    , Cmd.none
                    )

                Ok (Click action) ->
                    ( { model
                        | currentAction = Just (Click action)
                      }
                    , Cmd.none
                    )

                Ok (SpacialSearch action) ->
                    ( { model
                        | currentAction = Just (SpacialSearch action)
                      }
                    , Cmd.none
                    )

                Err msg ->
                    Debug.crash ("Error decoding Json websocket message: " ++ msg)

        FindTextUpdate response ->
            let
                log =
                    if String.startsWith "Success" response.result then
                        Debug.log "found element" response
                    else
                        response

                cmdModelPair =
                    if String.startsWith "Success" response.result then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeFindTextResult response) )
                    else if String.startsWith "Failure" response.result && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeFindTextResult response) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        ClickSearchUpdate result ->
            let
                log =
                    if String.startsWith "Success" result.result then
                        Debug.log "found element and clicked it" result
                    else
                        result

                cmdModelPair =
                    if String.startsWith "Success" result.result then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult result) )
                    else if String.startsWith "Failure" result.result && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult result) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        SpacialSearchUpdate result ->
            let
                log =
                    if String.startsWith "Success" result.result then
                        Debug.log "found in spacial search" result
                    else
                        result

                cmdModelPair =
                    if String.startsWith "Success" result.result then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeSpacialSearchResult result) )
                    else if String.startsWith "Failure" result.result && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeSpacialSearchResult result) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        UpdateTick time ->
            case model.currentAction of
                Just (FindText action) ->
                    let
                        cmd =
                            findText_search action.text

                        newModel =
                            case model.timeoutTime of
                                Nothing ->
                                    { model | timeoutTime = Just (time + timeoutInSeconds * second) }

                                Just timeoutTime ->
                                    if (time - timeoutTime) >= 0 then
                                        { model | timeout = True }
                                    else
                                        model
                    in
                        ( newModel, cmd )

                Just (Click action) ->
                    let
                        cmd =
                            click_searchText (clickPortData action.digId action.prevQueries)

                        newModel =
                            case model.timeoutTime of
                                Nothing ->
                                    { model | timeoutTime = Just (time + timeoutInSeconds * second) }

                                Just timeoutTime ->
                                    if (time - timeoutTime) >= 0 then
                                        { model | timeout = True }
                                    else
                                        model
                    in
                        ( newModel, cmd )

                Just (SpacialSearch action) ->
                    let
                        cmd =
                            spacialSearch (spacialSearchPortData action)

                        newModel =
                            case model.timeoutTime of
                                Nothing ->
                                    { model | timeoutTime = Just (time + timeoutInSeconds * second) }

                                Just timeoutTime ->
                                    if (time - timeoutTime) >= 0 then
                                        { model | timeout = True }
                                    else
                                        model
                    in
                        ( newModel, cmd )

                _ ->
                    ( model, Cmd.none )

        WebsiteLoaded ->
            case model.currentAction of
                Just (GoTo action) ->
                    ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult basicSuccessResult) )

                _ ->
                    ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ WebSocket.listen digServer NewMessage
        , Time.every (250 * millisecond) UpdateTick
        , findText_searchResult FindTextUpdate
        , click_searchResult ClickSearchUpdate
        , spacialSearch_result SpacialSearchUpdate
        ]



-- VIEW


view : Model -> Html Msg
view model =
    let
        iframeProps =
            List.concat
                [ [ id "siteUnderTest"
                  , style
                        [ ( "width", "100%" )
                        , ( "height", "100%" )
                        , ( "border", "0" )
                        ]
                  , onIFrameLoad WebsiteLoaded
                  ]
                , if String.isEmpty model.websiteUrl then
                    []
                  else
                    [ src model.websiteUrl ]
                ]
    in
        div
            [ style
                [ ( "height", "100%" ) ]
            ]
            [ div [ hidden True ] [ text "Elm Test Digger" ]
            , iframe iframeProps []
            ]


onIFrameLoad : msg -> Attribute msg
onIFrameLoad message =
    on "load" (Json.succeed message)
