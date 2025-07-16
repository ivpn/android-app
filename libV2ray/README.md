# libV2ray

A Go library for V2Ray core functionality, designed for Android mobile applications. This library bridges V2Ray's networking capabilities with Android apps through an Android Archive (AAR) package.

## Overview

libV2ray is developed by IVPN Limited, enabling Android applications to integrate V2Ray's proxy and tunneling capabilities with minimal configuration.

## Features

- Cross-platform compatibility with Go and gomobile
- V2Ray Core Integration (v5.33.0)
- Lightweight AAR package optimized for mobile
- Android API 21+ support

## Build Requirements

- **Go**: 1.24.3+
- **JDK**: 8+
- **Android SDK**: With build tools
- **gomobile**: Go mobile toolkit

### Environment Setup
```bash
export ANDROID_HOME=/path/to/android/sdk
export ANDROID_SDK_ROOT=/path/to/android/sdk
```

## Build Instructions

### Quick Build
```bash
git clone https://github.com/ivpn/libV2ray.git
cd libV2ray
chmod +x build.sh
./build.sh
```

## Integration

1. Copy `libv2ray.aar` to your Android project's `libs/` directory
2. Add to `build.gradle`: `implementation files('libs/libv2ray.aar')`
3. Configure required permissions in `AndroidManifest.xml`

## Credits

This project is built upon [V2Ray Core](https://github.com/v2fly/v2ray-core), a platform for building proxies to bypass network restrictions, developed by the V2Fly community.

### Acknowledgments

- All contributors to the V2Ray and V2Fly projects.
- The open-source community for their invaluable support and contributions.
