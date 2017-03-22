-- Read more about this program in the official Elm guide:
-- https://guide.elm-lang.org/architecture/effects/web_sockets.html


module Main exposing (..)

import Actions exposing (ActionType(Click, FindText, GoTo, SpacialSearch), ClickAction, FindTextAction, GoToAction, SpacialSearchAction, actionTypeDecoder, clickActionDecoder, findTextActionDecoder, goToActionDecoder, spacialSearchActionDecoder)
import Ports.Click exposing (click_searchResult, click_searchText)
import Ports.FindText exposing (findText_search, findText_searchResult)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Json.Decode as Json
import Ports.Model exposing (clickPortData, spacialSearchPortData)
import Ports.SpacialSearch exposing (spacialSearch, spacialSearch_result)
import Queries exposing (QueryType(Text))
import Response exposing (ActionResult, FindTextSearchResult, SpacialSearchResult, TestResultJS, actionResult, basicSuccessResult, encodeFindTextResult, encodeJsonResult, encodeSpacialSearchResult)
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
    , currentAction : Maybe ActionType
    , findTextAction : Maybe FindTextAction
    , clickAction : Maybe ClickAction
    , spacialSearchAction : Maybe SpacialSearchAction
    , timeoutTime : Maybe Time
    , timeout : Bool
    }


clearCurrentAction : Model -> Model
clearCurrentAction existingModel =
    { existingModel
        | websiteUrl = existingModel.websiteUrl
        , currentAction = Nothing
        , findTextAction = Nothing
        , clickAction = Nothing
        , spacialSearchAction = Nothing
        , timeoutTime = Nothing
        , timeout = False
    }


init : ( Model, Cmd Msg )
init =
    ( { websiteUrl = ""
      , currentAction = Nothing
      , findTextAction = Nothing
      , clickAction = Nothing
      , spacialSearchAction = Nothing
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
    | ClickSearchUpdate TestResultJS
    | SpacialSearchUpdate SpacialSearchResult


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NewMessage str ->
            case (Json.decodeString actionTypeDecoder str) of
                Ok GoTo ->
                    case (Json.decodeString goToActionDecoder str) of
                        Ok action ->
                            ( { model
                                | websiteUrl = action.uri
                                , currentAction = Just GoTo
                              }
                            , Cmd.none
                            )

                        Err msg ->
                            Debug.crash ("Error decoding Json GoToAction object: " ++ msg)

                Ok FindText ->
                    case (Json.decodeString findTextActionDecoder str) of
                        Ok action ->
                            ( { model
                                | currentAction = Just FindText
                                , findTextAction = Just action
                              }
                            , Cmd.none
                            )

                        Err msg ->
                            Debug.crash ("Error decoding Json FindTextAction object: " ++ msg)

                Ok Click ->
                    case (Json.decodeString clickActionDecoder str) of
                        Ok action ->
                            ( { model
                                | currentAction = Just Click
                                , clickAction = Just action
                              }
                            , Cmd.none
                            )

                        Err msg ->
                            Debug.crash ("Error decoding Json ClickAction object: " ++ msg)

                Ok SpacialSearch ->
                    case (Json.decodeString spacialSearchActionDecoder str) of
                        Ok action ->
                            ( { model
                                | currentAction = Just SpacialSearch
                                , spacialSearchAction = Just action
                              }
                            , Cmd.none
                            )

                        Err msg ->
                            Debug.crash ("Error decoding Json SpacialSearchAction object: " ++ msg)

                Err msg ->
                    Debug.crash ("Error decoding Json websocket message: " ++ msg)

        FindTextUpdate search ->
            let
                log =
                    if search.result == "Success" then
                        Debug.log "found element" search
                    else
                        search

                response =
                    { result = search.result
                    , digId = search.digId
                    , closestMatches = search.closestMatches
                    }

                cmdModelPair =
                    if search.result == "Success" then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeFindTextResult response) )
                    else if search.result == "Failure" && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeFindTextResult response) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        ClickSearchUpdate search ->
            let
                log =
                    if search.result == "Success" then
                        Debug.log "found element and clicked it" search
                    else
                        search

                response =
                    { result = actionResult search.result
                    , message = search.message
                    }

                cmdModelPair =
                    if search.result == "Success" then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult response) )
                    else if search.result == "Failure" && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult response) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        SpacialSearchUpdate result ->
            let
                log =
                    if result.result == "Success" then
                        Debug.log "found in spacial search" result
                    else
                        result

                cmdModelPair =
                    if result.result == "Success" then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeSpacialSearchResult result) )
                    else if result.result == "Failure" && model.timeout then
                        ( clearCurrentAction model, WebSocket.send digServer (encodeSpacialSearchResult result) )
                    else
                        ( model, Cmd.none )
            in
                cmdModelPair

        UpdateTick time ->
            case model.currentAction of
                Just FindText ->
                    let
                        cmd =
                            case model.findTextAction of
                                Just action ->
                                    findText_search action.text

                                Nothing ->
                                    Cmd.none

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

                Just Click ->
                    let
                        cmd =
                            case model.clickAction of
                                Just action ->
                                    click_searchText (clickPortData action.digId action.prevQueries)

                                Nothing ->
                                    Debug.crash "Unexpected: action state was empty while processing Click action"

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

                Just SpacialSearch ->
                    let
                        cmd =
                            case model.spacialSearchAction of
                                Just action ->
                                    spacialSearch (spacialSearchPortData action)

                                Nothing ->
                                    Debug.crash "Unexpected: action state was empty while processing SpacialSearch action"

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
            if model.currentAction == Just GoTo then
                ( clearCurrentAction model, WebSocket.send digServer (encodeJsonResult basicSuccessResult) )
            else
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
