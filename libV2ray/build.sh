#!/bin/bash

set -e  

# Build script for libV2ray Android AAR
# This script builds an Android Archive (AAR) file from the Go libV2ray library
# 
# Copyright (c) IVPN Limited
# Licensed under the GPLv3: https://www.gnu.org/licenses/gpl-3.0.html

echo "Starting libV2ray AAR build process..."

# Build Configuration
PACKAGE_NAME="github.com/ivpn/libV2ray/libV2ray"
OUTPUT_DIR="build"
AAR_NAME="libv2ray"
MIN_SDK_VERSION="21"
TARGET_SDK_VERSION="34"
DEST_DIR="../core/libs"

# ANSI Color Codes for Terminal Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Logging Functions
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate Go Installation
if ! command -v go &> /dev/null; then
    print_error "Go is not installed. Please install Go before proceeding."
    exit 1
fi

print_status "Go version: $(go version)"

# Validate and Install gomobile
if ! command -v gomobile &> /dev/null; then
    print_warning "gomobile not found. Installing gomobile..."
    go install golang.org/x/mobile/cmd/gomobile@latest
    if ! command -v gomobile &> /dev/null; then
        print_error "Failed to install gomobile. Please verify Go installation and GOPATH configuration."
        exit 1
    fi
fi

print_status "gomobile found: $(which gomobile)"

# Initialize gomobile environment
print_status "Initializing gomobile..."
gomobile init

# Validate Android SDK Installation
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    print_warning "ANDROID_HOME or ANDROID_SDK_ROOT environment variables not set. Attempting to locate Android SDK..."

    # Standard Android SDK installation paths
    POSSIBLE_ANDROID_HOMES=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "/usr/local/android-sdk"
        "/opt/android-sdk"
    )

    for sdk_path in "${POSSIBLE_ANDROID_HOMES[@]}"; do
        if [ -d "$sdk_path" ]; then
            export ANDROID_HOME="$sdk_path"
            export ANDROID_SDK_ROOT="$sdk_path"
            print_status "Android SDK located at: $sdk_path"
            break
        fi
    done

    if [ -z "$ANDROID_HOME" ]; then
        print_error "Android SDK not found. Please install Android SDK and configure ANDROID_HOME environment variable."
        exit 1
    fi
fi

print_status "Android SDK: $ANDROID_HOME"

# Prepare Build Environment
mkdir -p "$OUTPUT_DIR"

# Clean Previous Build Artifacts
print_status "Cleaning previous build artifacts..."
rm -rf "$OUTPUT_DIR"/*.aar
rm -rf "$OUTPUT_DIR"/*.jar

# Download and Validate Dependencies
print_status "Downloading Go dependencies..."
go mod download
go mod tidy

# Execute AAR Build Process
print_status "Building AAR for Android..."
print_status "Package: $PACKAGE_NAME"
print_status "Output: $OUTPUT_DIR/$AAR_NAME.aar"
print_status "Minimum SDK: $MIN_SDK_VERSION"
print_status "Target SDK: $TARGET_SDK_VERSION"

# Configure Build Environment
export CGO_ENABLED=1

# Execute gomobile bind command
gomobile bind \
    -target=android \
    -androidapi="$MIN_SDK_VERSION" \
    -o "$OUTPUT_DIR/$AAR_NAME.aar" \
    -v \
    "$PACKAGE_NAME"

# Validate Build Success
print_status "AAR build completed successfully"
print_status "Output file: $OUTPUT_DIR/$AAR_NAME.aar"

# Display build artifact information
FILE_SIZE=$(ls -lh "$OUTPUT_DIR/$AAR_NAME.aar" | awk '{print $5}')
print_status "File size: $FILE_SIZE"

# Remove old AAR from ./core/libs
print_status "Removing old AAR files from $DEST_DIR..."
mkdir -p "$DEST_DIR"
rm -f "$DEST_DIR"/*.aar

# Copy new AAR to ./core/libs
print_status "Copying new AAR to $DEST_DIR..."
cp "$OUTPUT_DIR/$AAR_NAME.aar" "$DEST_DIR/"

print_status "AAR file copied successfully to $DEST_DIR"

# Display AAR contents if unzip is available
if command -v unzip &> /dev/null; then
    print_status "AAR contents:"
    unzip -l "$OUTPUT_DIR/$AAR_NAME.aar" | head -20
fi

# Generate Build Metadata
BUILD_INFO_FILE="$OUTPUT_DIR/build_info.txt"
cat > "$BUILD_INFO_FILE" << EOF
libV2ray AAR Build Information
==============================
Build Date: $(date)
Go Version: $(go version)
Gomobile Version: $(gomobile version 2>/dev/null || echo "Unknown")
Android SDK: $ANDROID_HOME
Minimum SDK Version: $MIN_SDK_VERSION
Target SDK Version: $TARGET_SDK_VERSION
Package: $PACKAGE_NAME
Output: $AAR_NAME.aar
File Size: $(ls -lh "$OUTPUT_DIR/$AAR_NAME.aar" | awk '{print $5}')

Build Command Used:
gomobile bind -target=android -androidapi=$MIN_SDK_VERSION -o $OUTPUT_DIR/$AAR_NAME.aar -v $PACKAGE_NAME
EOF

print_status "Build metadata saved to: $BUILD_INFO_FILE"

echo ""
print_status "Build process completed successfully"
print_status "AAR file available at: $OUTPUT_DIR/$AAR_NAME.aar"
print_status ""
print_status "Integration Instructions:"
print_status "1. Copy the AAR file to your Android project's libs/ directory"
print_status "2. Add the following dependency to your app's build.gradle:"
print_status "   implementation files('libs/$AAR_NAME.aar')"
print_status "3. Ensure required permissions are declared in AndroidManifest.xml"
print_status ""
print_status "Build completed successfully"
