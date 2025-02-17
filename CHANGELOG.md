# Changelog

All notable changes to this project will be documented in this file.

## Version 2.10.12 - 2025-02-17

[FIXED] Initial payment error for existing accounts  

## Version 2.10.11 - 2025-01-22

[IMPROVED] Account ID input type has been updated from "VisiblePassword" to "Password" for enhanced security  
[IMPROVED] Obsolete v1 Signature Scheme has been removed to maintain compatibility with modern standards  

[Download IVPN Client v2.10.11](https://www.ivpn.net/releases/android/IVPNv2.10.11site.apk)  
SHA256: bfca85b2fa512e1178857d35218ea2b765f52cdc1b12e85b2108c0b52c2e786b  

## Version 2.10.10 - 2024-08-20

[FIXED] Crash when using Network Protection  
[FIXED] Crash when using the Update button  
[FIXED] Connect VPN using the Quick Settings tile  
[NOTE] Removed option to report crash logs with Sentry  

[Download IVPN Client v2.10.10](https://www.ivpn.net/releases/android/IVPNv2.10.10site.apk)  
SHA256: 484ede3adabda94bc78768d3f3b1dc12732cbb76eb886216bc693850a469f5fe 

## Version 2.10.9 - 2024-08-08

[IMPROVED] Target Android API level 34  
[FIXED] 2FA login not working when session limit is reached  

[Download IVPN Client v2.10.9](https://www.ivpn.net/releases/android/IVPNv2.10.9site.apk)  
SHA256: cd68f954ec349277bec818988c57494a7f64f4447d0c5804607c95977a1b1db2  

## Version 2.10.8 - 2024-06-03

[IMPROVED] Post-Quantum library updated to the latest version  
[IMPROVED] Support for pending payments  
[NOTE] Removed support for Android 7.0 and older  

[Download IVPN Client v2.10.8](https://www.ivpn.net/releases/android/IVPNv2.10.8site.apk)  
SHA256: 5f3de49a59b80f06087f440d3e7d939d2f815d7ba944a1a75c90a20e447865cd  

## Version 2.10.7 - 2024-02-12

[NEW] Device Management  
[IMPROVED] Increased timeout for API requests  

[Download IVPN Client v2.10.7](https://www.ivpn.net/releases/android/IVPNv2.10.7site.apk)  
SHA256: e091ee87d73eda39036854ca02be2c0451502730043fe39a8242403124965ceb  

## Version 2.10.6 - 2023-12-18

[FIXED] Crash when opening the app on F-Droid  

## Version 2.10.5 - 2023-12-13

[IMPROVED] Show non-launchable and system apps in the Split Tunneling list  
[IMPROVED] Update WireGuard to the latest version  
[IMPROVED] Crash logging option disabled by default  

[Download IVPN Client v2.10.5](https://www.ivpn.net/releases/android/IVPNv2.10.5site.apk)  
SHA256: bdf7c6e4191f4ad175c752eb78437d17bc99c2c907dd0d887fa3cb8a46c402a9  

## Version 2.10.4 - 2023-09-18

[IMPROVED] Upgraded payment library  
[FIXED] App notifications not working on Android 13  

[Download IVPN Client v2.10.4](https://www.ivpn.net/releases/android/IVPNv2.10.4site.apk)  
SHA256: 4f0827db0cfcac596a87d204eb8d9f128cb6d493cdbb57070afdd7a71e9d6472  

## Version 2.10.3 - 2023-09-06

[IMPROVED] Refactored rules for LAN access  
[IMPROVED] Removed possibility to connect to OpenVPN server using hostname  
[IMPROVED] Target Android API level 33  

[Download IVPN Client v2.10.3](https://www.ivpn.net/releases/android/IVPNv2.10.3site.apk)  
SHA256: 936d4b6520d93e1fee06d334b4fec4a4185e8d8c1059d23fa322b4005b718179  

## Version 2.10.1 - 2023-08-15

[FIXED] App crashing on 32-bit devices due to missing liboqs library  
[FIXED] Minor UI fixes  

[Download IVPN Client v2.10.1](https://www.ivpn.net/releases/android/IVPNv2.10.1site.apk)  
SHA256: 049d39816d3f776ad901744d9f14227460de248d134a13f447b4a736f8a5d6d2  

## Version 2.10.0 - 2023-08-07

[NEW] Post-Quantum Resistant WireGuard Connections  
[NEW] AntiTracker Plus additional block lists  
[FIXED] Minor UI fixes  

[Download IVPN Client v2.10.0](https://www.ivpn.net/releases/android/IVPNv2.10.0site.apk)  
SHA256: d01e664ce45898fe75b293fae60f705f6f6cddf6f0d44084a9404910b2caa712  

## Version 2.9.0 - 2023-06-29

[NEW] Custom ports  
[NEW] Support for Android 13 Themed Icons  
[IMPROVED] OpenSSL updated to 1.1.1l  
[NOTE] Removed references to Port Forwarding  

[Download IVPN Client v2.9.0](https://www.ivpn.net/releases/android/IVPNv2.9.0site.apk)  
SHA256: 72842519c72d837ea0831db1f33c89dbf359a753d2d067c99e5ca051c0c75c54  

## Version 2.8.6 - 2023-04-13

[IMPROVED] Use the same server settings for both OpenVPN and WireGuard  
[IMPROVED] By default, connect OpenVPN using IP address instead of DNS hostname  
[IMPROVED] Small UI improvements  
[FIXED] Issue with no traffic when using OpenVPN MultiHop with AntiTracker  
[FIXED] Resolved app crashes  

[Download IVPN Client v2.8.6](https://www.ivpn.net/releases/android/IVPNv2.8.6site.apk)  
SHA256: e27ca4b9d64b012d1ebbba6be27d8a3d1f43abe544165240f10c571873e09822  

## Version 2.8.5 - 2023-04-02

[IMPROVED] Update Android Gradle plugin  

## Version 2.8.4 - 2023-01-24

[FIXED] South Africa server label missing in the map  
[FIXED] Server latency information not updated when disconnecting from VPN  
[FIXED] Connection remains paused after logout  
[FIXED] Fastest server configuration not working  
[FIXED] Minor UI fixes  
[FIXED] Crash when parsing server info  
[FIXED] Crash when populating installed apps for Split Tunneling  
[FIXED] Crash when redirecting legacy account to website signup  
[FIXED] Crash on initial Play Store payment  

[Download IVPN Client v2.8.4](https://www.ivpn.net/releases/android/IVPNv2.8.4site.apk)  
SHA256: b78256459903fef2ec23608792ad030a9581fd0e34620080270005222524fdac  

## Version 2.8.3 - 2022-06-13

[IMPROVED] New implementation for OpenVPN Multi-Hop  
[IMPROVED] New ports for WireGuard and OpenVPN  
[FIXED] Minor UI fixes  

[Download IVPN Client v2.8.3](https://www.ivpn.net/releases/android/IVPNv2.8.3site.apk)  
SHA256: 8569cff849bcf27667f95a8f9f89ac1ca297376b6f77e68608f85438fea4fa26  

## Version 2.8.2 - 2022-04-05

[IMPROVED] Mitigate StrandHogg vulnerability  
[IMPROVED] Explicitly disable all clear-text HTTP communications  
[IMPROVED] Disable v1 APK signature  

[Download IVPN Client v2.8.2](https://www.ivpn.net/releases/android/IVPNv2.8.2site.apk)  
SHA256: 229e3d5f64cf892f48d59473413e18375527939550634804637da70303b321aa    

## Version 2.8.1 - 2021-11-30

[FIXED] User is not logged out when session is removed  
[FIXED] Exit server is not displayed in the notifications area  
[FIXED] Random exit server does not work with WireGuard Multi-Hop  

[Download IVPN Client v2.8.1](https://www.ivpn.net/releases/android/IVPNv2.8.1site.apk)  
SHA256: ddcff86bfa3035cae3f4af3ce7fc4d699caa0a5eba849f8526c94f8a7fe4bf03    

## Version 2.8.0 - 2021-11-05

[NEW] Multi-Hop for WireGuard protocol  
[NEW] Option to keep app settings on logout  
[NEW] Server sorting by proximity  
[IMPROVED] Descriptions and UI overall  
[FIXED] IVPN tile in the quick settings  

[Download IVPN Client v2.8.0](https://www.ivpn.net/releases/android/IVPNv2.8.0site.apk)  
SHA256: c3b6c717be41f3a231dbf576525910204cb72a25aaaa8a4106d312a3cd16d736  

## Version 2.7.0(2.7.1) - 2021-10-19

[Remove] In-app Kill Switch implementation

[Download IVPN Client v2.7.1](https://cdn.ivpn.net/releases/android/IVPNv2.7.1site.apk)  
SHA256: 54f3bd56856c08ab55b9b81b536f1c92fca54a51bf4ba5b456cca365e9fc1f4f  

## Version 2.6.0 - 2021-07-07

[NEW] Ability to zoom map in/out  

[Download IVPN Client v2.6.0](https://cdn.ivpn.net/releases/android/IVPNv2.6.0site.apk)  
SHA256: 1e1e196dd159d083eb19570a95567b9392fdbea086226696bedfed06f53a8d4c

## Version 2.5.0(2.5.1-2.5.2) - 2021-06-30

[NEW] IPv6 inside WireGuard tunnel  
[NEW] IPv6 connection info  

[Download IVPN Client v2.5.2](https://cdn.ivpn.net/releases/android/IVPNv2.5.2site.apk)  
SHA256: 244afbe2f7414d76796bfb34f9bada6c7b177265f591a6194a3b4ce829c3319c

## Version 2.4.0(2.4.1) - 2021-04-16

[NEW] Possibility to mock GPS location  
[NEW] Bypass VPN for local networks  
[IMPROVED] UI/UX for settings  
[FIXED] Kill switch state on applying network protection rules  
[FIXED] UI/UX for in-app purchases for the existing accounts  

[Download IVPN Client v2.4.1](https://cdn.ivpn.net/releases/android/IVPNv2.4.1site.apk)  
SHA256: 3fa89f3ee041c388bf596348c1acb3ae3871d22aa750db0bd71538a28441488e

## Version 2.3.0(2.3.1) - 2021-02-25

[NEW] Possibility to establish VPN connection via Android Quick Setting Tile  
[NEW] Search on the Split tunneling screen  
[NEW] Application icon  
[IMPROVED] Kill switch user interface  
[IMPROVED] WireGuard is now default protocol  

[Download IVPN Client v2.3.0](https://cdn.ivpn.net/releases/android/IVPNv2.3.0site.apk)  
SHA256: bdad20ef620d6725c9339bf8ad74f65075cd684be75deb8efb026bb92c023567

## Version 2.2.0 - 2021-01-29

[NEW] Updated Privacy Policy and Terms of Service  
[NEW] Certificate pinning to prevent man-in-the-middle attacks  
[NEW] Support two-factor authentication for login  
[NEW] Support captcha to mitigate attacks  

[Download IVPN Client v2.2.0](https://cdn.ivpn.net/releases/android/IVPNv2.2.0_site.apk)  
SHA256: c1c9b84a45c3ed98d4cce15f12c887cdbe84dcd00a3b8046ebfe6c75a073be67

## Version 2.1.0 - 2020-12-01

[New] Antitracker switcher state for hardcore mode  
[New] Provide additional information for a subscription plan  
[Improved] Date format  
[Improved] Cases when the Antitracker can be enabled  
[Improved] Random server selection logic  
[Improved] Map animation  
[Improved] Optimize map size, removed unused parts  
[Improved] Update the WireGuard library to the latest  
[Fixed] VPN connection counted as metered  
[Fixed] Crash on destroying the server list  
[Fixed] Crashes on double navigation actions  

[Download IVPN Client v2.1.0](https://cdn.ivpn.net/releases/android/IVPNv2.1.0site.apk)  
SHA256: b07512e5e27b336bd7a9f5098b2f9db76ec481d40236416dc417671724c3e05e

## Version 2.0.2 - 2020-10-12

[IMPROVED] Display connection status  
[FIXED] Network protection in background state  
[FIXED] Missing applications for Split tunneling  
[FIXED] Crash while manipulating map  
[FIXED] Crash when canceling login or other communication with server  
[FIXED] Crash trying to open dialog while app is in background state  

[Download IVPN Client v2.0.2](https://cdn.ivpn.net/releases/android/IVPNv2.0.2site.apk)  
SHA256: 751a7db841e85d749b6ab908aaa9c983a7375f404afdc9221cdc3a371af545ad

## Version 2.0.0 - 2020-09-29

[NEW] Redesigned UI  
[NEW] Interactive map  
[NEW] Control panel  
[NEW] Dedicated account screen  
[NEW] Dark theme  
[IMPROVED] Search and sort on the servers list  

[Download IVPN Client v2.0.0](https://cdn.ivpn.net/releases/android/IVPNv2.0site.apk)  
SHA256: f867b5176dd028abe75826b1488f689406303ec93abe44d5b971edc30bbac06d

## Version 1.68 - 2020-06-24

[Improved] Account Id validation logic  

[Download IVPN Client v1.68](https://cdn.ivpn.net/releases/android/IVPNv1.68site.apk)  
SHA256: 2817cf67578592d6a24248de50cd5d1f15e2063d2ae62e0340ba472f99c755a6

## Version 1.67.3 - 2020-05-05

[Improved] Control channel cipher for OpenVPN  
[Improved] Remove WireGuard beta warning  
[New] Implement load balancer for WireGuard

[Download IVPN Client v1.67.3](https://cdn.ivpn.net/releases/android/IVPNv1.67.3site.apk)  
SHA256: d931b00defeafbcd08037c9d4fa244fc6750c67902ce93dff643d0bb6fe6c7e7

## Version 1.66.2 - 2020-04-16

[Improved] OpenVPN certificate authority was updated  
[Improved] OpenVPN and OpenSSL libraries were upgraded to the latest  
[Improved] WireGuard library was upgraded to the latest  

[Download IVPN Client v1.66.2](https://cdn.ivpn.net/releases/android/IVPNv1.66.2site.apk)  
SHA256: cdb2071c29ea805494b8c608a6c585abd7e6e99724d84341cb2eb9cd576e446c  

## Version 1.65 - 2020-02-27

[NEW] Option to enable/disable sending crash reports  
[IMPROVED] Migrated Android SDK to androidx  
[IMPROVED] Logic for start on boot. Feature is now disabled for Android 10, due to OS restrictions. Please use Always-on VPN  
[IMPROVED] Logic of all services for Android 10  
[IMPROVED] WireGuard library was upgraded to the latest version  
[IMPROVED] Subscription logic for edge cases  
[IMPROVED] Overall stability  
[FIXED] Network protection for some cases  
[FIXED] Always-on VPN for Android 10  
[FIXED] Logic for sending crashes for Android 10  
[FIXED] UI issues for devices with small screen  

## Version 1.64 - 2020-01-20

[IMPROVED] Overall stability  

## Version 1.63 - 2019-12-16

[IMPROVED] Overall stability  

## Version 1.62 - 2019-12-12

[IMPROVED] Login screen  

## Version 1.58 - 2019-11-14

[IMPROVED] Overall stability  
[FIXED] Real WireGuard regeneration period didn't match one shown on UI  
[FIXED] Sudden logouts after closing the application  
[FIXED] Application UI state when the paused connection was stopped.  

## Version 1.57 - 2019-11-13

[NEW] Bypass DNS blocks to IVPN API  
[IMPROVED] Login session management  

## Version 1.56 - 2019-09-24

[FIXED] Wrong connection status in some circumstances  

## Version 1.55 - 2019-08-28

[FIXED] Network protection not working in some circumstances  

## Version 1.54 - 2019-08-21

[FIXED] WireGuard keys updating too often in some circumstances  

## Version 1.53 - 2019-08-07

[IMPROVED] Overall stability  
[FIXED] Auto-update not working in some circumstances  

## Version 1.52 - 2019-07-29

[NEW] Added auto-update feature to notify when the newest version is available  
[NEW] Added new port for connection: 1194 UDP  
[FIXED] DNS issue with Multi-Hop connection when AntiTracker enabled  
[FIXED] Out of memory issue  
[FIXED] Various UI issues  

## Version 1.50 - 2019-06-11

[FIXED] Various UI issues  

## Version 1.49 - 2019-06-11

[NEW] AntiTracker: block ads, malicious websites, and third-party trackers  
[NEW] Custom DNS: specify DNS server when connected to VPN  
[NEW] Automatic WireGuard key regeneration  

## Version 1.48 - 2019-05-14

[NEW] Fastest server configuration  
[NEW] Application shortcuts  
[NEW] Display public IP and geolocation information  
[IMPROVED] Stability of WireGuard connection  
[IMPROVED] Start on device boot  
[IMPROVED] Always-on VPN  
