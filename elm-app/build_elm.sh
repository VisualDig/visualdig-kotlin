#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
ELM_BIN="$(npm bin)";

pushd $DIR
[ ! -d "elm-stuff" ] && $ELM_BIN/elm-package install --yes
$ELM_BIN/elm-make src/Main.elm --output assets/elm.js --yes
popd
