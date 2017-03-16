#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
NPM_BIN="$(npm bin)";

pushd $DIR

npm install

if [ $? -eq 0 ]
then
  echo "NPM install complete."
else
  echo "Failed to perform npm install command" >&2
  exit 1
fi


if [ ! -d "elm-stuff" ]
then
  $NPM_BIN/elm-package install --yes

  if [ $? -eq 0 ]
  then
    echo "Compiling and installing elm packages completed!"
  else
    echo "Failed to install elm packages." >&2
    exit 2
  fi
fi


$NPM_BIN/elm-make src/Main.elm --output js/elm.js --yes

if [ $? -eq 0 ]
then
  echo "Compiling elm front-end completed succesfully!"
else
  echo "Failed to compile elm application" >&2
  exit 3
fi


$NPM_BIN/webpack

if [ $? -eq 0 ]
then
  echo "Webpack compilation finished!"
else
  echo "Failed to compile JS to a webpack bundle" >&2
  exit 4
fi


popd
