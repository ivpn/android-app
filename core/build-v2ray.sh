#!/bin/bash

set -e

mkdir -p libs

echo "=> Get gomobile.."
cd v2RayControl
PATH=$PATH:~/go/bin
go get golang.org/x/mobile/cmd/gomobile

echo "=> Build Android library.."
OUT_AAR=../libs/V2RayControl.aar
gomobile init &&
gomobile bind -trimpath -ldflags "-s -w" -target=android -o ${OUT_AAR}
echo "=> Android build completed (out: ${OUT_AAR})"
