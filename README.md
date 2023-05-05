# AccelPunch_application
AccelPunch: Simple boxing performance monitoring application for Android

**Platform:** Android

> Compiled using Android Studio (min SDK used and tested: 29)

To establish connection with nodes, initialize hotspot with SSID `AccelPunch-AP` and password `accelpunch` (default _AccelPunch_node_ config)

To establish connection with server, specify local/external network IP in text input on startup or by pressing **PING** (if dialog was dismissed) or **SET IP**.

## Usage notes

Usually hotspot and local Wi-Fi don't work simultaneously because of hardware limitations. Because of that application saves your performance in batches in persisting storage and _[TODO: does that until connection with server is established]_. So after each boxing session you can disconnect from nodes by connecting to the server to send the readings and then reconnect again by enabling hotspot - connections with nodes will be established automatically.

Or host servlet publicly.

Or use device that can perform Wi-Fi sharing.

## Data Origin

[AccelPunch_node](https://github.com/Pzkane/AccelPunch_node)

## Data Receiver

[AccelPunch_server](https://github.com/Pzkane/AccelPunch_server)