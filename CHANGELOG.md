# Changelog

All notable changes to this project will be documented in this file.

## Version 2.2.0 - 2021-01-29

[NEW] Updated Privacy Policy and Terms of Service  
[NEW] Certificate pinning to prevent man-in-the-middle attacks  
[NEW] Support two-factor authentication for login  
[NEW] Support captcha to mitigate attacks  

[Download IVPN Client v. 2.2.0](https://cdn.ivpn.net/releases/android/IVPNv2.2.0_site.apk)
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

[Download IVPN Client v. 2.1.0](https://cdn.ivpn.net/releases/android/IVPNv2.1.0site.apk)
SHA256: b07512e5e27b336bd7a9f5098b2f9db76ec481d40236416dc417671724c3e05e

## Version 2.0.2 - 2020-10-12

[IMPROVED] Display connection status  
[FIXED] Network protection in background state  
[FIXED] Missing applications for Split tunneling  
[FIXED] Crash while manipulating map  
[FIXED] Crash when canceling login or other communication with server  
[FIXED] Crash trying to open dialog while app is in background state  

[Download IVPN Client v. 2.0.2](https://cdn.ivpn.net/releases/android/IVPNv2.0.2site.apk)  
SHA256: 751a7db841e85d749b6ab908aaa9c983a7375f404afdc9221cdc3a371af545ad

## Version 2.0.0 - 2020-09-29

[NEW] Redesigned UI  
[NEW] Interactive map  
[NEW] Control panel  
[NEW] Dedicated account screen  
[NEW] Dark theme  
[IMPROVED] Search and sort on the servers list  

[Download IVPN Client v. 2.0.0](https://cdn.ivpn.net/releases/android/IVPNv2.0site.apk)  
SHA256: f867b5176dd028abe75826b1488f689406303ec93abe44d5b971edc30bbac06d

## Version 1.68 - 2020-06-24

[Improved] Account Id validation logic  

[Download IVPN Client v. 1.68](https://cdn.ivpn.net/releases/android/IVPNv1.68site.apk)  
SHA256: 2817cf67578592d6a24248de50cd5d1f15e2063d2ae62e0340ba472f99c755a6

## Version 1.67.3 - 2020-05-05

[Improved] Control channel cipher for OpenVPN  
[Improved] Remove WireGuard beta warning  
[New] Implement load balancer for WireGuard

[Download IVPN Client v. 1.67.3](https://cdn.ivpn.net/releases/android/IVPNv1.67.3site.apk)  
SHA256: d931b00defeafbcd08037c9d4fa244fc6750c67902ce93dff643d0bb6fe6c7e7

## Version 1.66.2 - 2020-04-16

[Improved] OpenVPN certificate authority was updated  
[Improved] OpenVPN and OpenSSL libraries were upgraded to the latest  
[Improved] WireGuard library was upgraded to the latest  

[Download IVPN Client v. 1.66.2](https://cdn.ivpn.net/releases/android/IVPNv1.66.2site.apk)  
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
