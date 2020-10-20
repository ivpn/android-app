# IVPN for Android

**IVPN for Android** is a native app built using Java and Kotlin languages. Some of the features include: multiple protocols (OpenVPN, WireGuard), Kill-switch, Multi-Hop, Trusted Networks, AntiTracker, Custom DNS, Always-on VPN, Start on boot and etc.
IVPN Android app is distributed on the [Google Play Store](https://play.google.com/store/apps/details?id=net.ivpn.client).

* [About this Repo](#about-repo)
* [Installation](#installation)
* [Deployment](#deployment)
* [Versioning](#versioning)
* [Contributing](#contributing)
* [Security Policy](#security)
* [License](#license)
* [Authors](#authors)
* [Acknowledgements](#acknowledgements)

<a name="about-repo"></a>
## About this Repo

This is the official Git repo of the [IVPN for Android](https://github.com/ivpn/android-app).

<a name="installation"></a>
## Installation

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Requirements

- Android SDK (can get from Android Studio)
- Java
- Kotlin (can get from Android Studio)
- Git
- Gradle

### Dependencies

Project dependencies:  

* [retrofit2](https://github.com/square/retrofit)
* [gson](https://github.com/google/gson)
* [streamsupport](https://github.com/streamsupport/streamsupport)
* [sentry-android](https://github.com/kishikawakatsumi/KeychainAccess)
* [slf4j](https://github.com/qos-ch/slf4j)
* [logback-android](https://github.com/tony19/logback-android)
* [mockito](https://github.com/mockito/mockito)
* [powermock](https://github.com/powermock/powermock)
* [dagger](https://github.com/google/dagger)
* [openvpn](https://github.com/schwabe/ics-openvpn)
* [wireguard](https://github.com/WireGuard/wireguard-android)

<a name="deployment"></a>
## Deployment

There are 2 different product flavours in the project. First - spread, which value can be "store" or "site". "Site" spread flavour contains additional features(like Antitracker) that are not allowed in Google Play Store. Also, the differences in payment methods.

Second product flavour - API, there are two of them: stage and production.

So based on flavours and build types(release, debug) eight active build variants are available:

```sh
siteProductionDebug
siteProductionRelease
siteStageDebug
siteStageRelease
storeProductionDebug
storeProductionRelease
storeStageDebug
storeStageRelease
```

You can use each of them to build the application by Gradle from the command line or from Android Studio.

<a name="versioning"></a>
## Versioning

Project is using versioning for creating release versions in the format:

`x.y`

`x` stands for a **major** version  
`y` stands for a **minor** version

<a name="contributing"></a>
## Contributing

If you are interested in contributing to IVPN for Android project, please read our [Contributing Guidelines](/.github/CONTRIBUTING.md).

<a name="security"></a>
## Security Policy

If you want to report a security problem, please read our [Security Policy](/.github/SECURITY.md).

<a name="license"></a>
## License

This project is licensed under the GPLv3 - see the [License](/LICENSE.md) file for details.

<a name="authors"></a>
## Authors

See the [Authors](/AUTHORS) file for the list of contributors who participated in this project.

<a name="acknowledgements"></a>
## Acknowledgements

See the [Acknowledgements](/ACKNOWLEDGEMENTS.md) file for the list of third party libraries used in this project.