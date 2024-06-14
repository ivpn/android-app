#!/bin/bash

set -e

# Make sure script will work the same if called from
# root directory or scripts directory
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"
mkdir -p libs

echo "=> Get gomobile.."
cd V2RayControl
PATH=$PATH:~/go/bin
go get golang.org/x/mobile/cmd/gomobile

echo "=> Build Android library.."
OUT_AAR=../libs/V2RayControl.aar
gomobile init &&
gomobile bind -trimpath -ldflags "-s -w" -target=android -androidapi 21 -o ${OUT_AAR}
# echo "=> Android build completed (out: ${OUT_AAR})"
