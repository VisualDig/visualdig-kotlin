-- Read more about this program in the official Elm guide:
-- https://guide.elm-lang.org/architecture/effects/web_sockets.html


module Main exposing (..)

import Actions exposing (ActionCommon, ActionType(GoTo), GoToAction, actionTypeDecoder, goToActionDecoder)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Json.Decode as Json
import Response exposing (encodeJsonResult, successResult)
import WebSocket


main =
    Html.program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


echoServer : String
echoServer =
    "ws://localhost:8650/dig"



-- MODEL


type alias Model =
    { websiteUrl : String
    , currentAction : Maybe ActionType
    }


init : ( Model, Cmd Msg )
init =
    ( { websiteUrl = "", currentAction = Nothing }
    , Cmd.none
    )



-- UPDATE


type Msg
    = NewMessage String
    | WebsiteLoaded


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        --    Send ->
        --      (Model "" messages, WebSocket.send echoServer input)
        NewMessage str ->
            case (Json.decodeString actionTypeDecoder str) of
                Ok (Just GoTo) ->
                    case (Json.decodeString goToActionDecoder str) of
                        Ok goToAction ->
                            ( { model
                                | websiteUrl = goToAction.uri
                                , currentAction = Just GoTo
                              }
                            , Cmd.none
                            )

                        Err msg ->
                            Debug.crash ("Error decoding Json GoToAction object: " ++ msg)

                Ok Nothing ->
                    Debug.crash ("Did not match known action: " ++ str)

                Err msg ->
                    Debug.crash ("Error decoding Json websocket message: " ++ msg)

        WebsiteLoaded ->
            if model.currentAction == Just GoTo then
                ( { model | currentAction = Nothing }, WebSocket.send echoServer (encodeJsonResult successResult) )
            else
                ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen echoServer NewMessage



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
