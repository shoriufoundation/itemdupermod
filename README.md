# ItemDuper (Only Singleplayer)

Client-side Fabric mod for Minecraft 26.2. The `/makeunlimited` client command toggles the item in your main hand. The selected stack's count is preserved when vanilla code shrinks it, while its ordinary use action still runs.

## Multiplayer safety

The command refuses to activate unless the client is connected to its integrated singleplayer server and that server has not been published to LAN. If LAN is opened while an item is marked, a client tick disables the feature and clears its marker from the player's inventory on both sides. The item hook also checks the private singleplayer state before preserving a stack, so consumption is allowed as soon as LAN is published. The mod metadata is client-only, so it cannot load on a dedicated server.

This is client-side behavior. It does not grant authority over remote servers or change server-side inventory rules.

## Tutorial
You can watch [the video](https://youtu.be/dv0Hc5KkWIk) for usage.

## Build

Install JDK 25 or newer and use the included Gradle wrapper:

```
.\gradlew.bat build
```

The distributable JAR is written to `build/libs/itemduper-1.0.0.jar`.

## Project details

- Minecraft: 26.2
- Java: 25
- Fabric Loader: 0.19.3
- Fabric Loom: 1.17-SNAPSHOT
- Fabric API: 0.156.0+26.2
- Package: `az.org.shoriufoundation.itemduper`
- Mod ID: `itemduper`

## Copyright

Copyright (c) 2026 Shoriu Foundation. All rights reserved.

Project name and organization details are configured for Shoriu Foundation. Add the Foundation's official website/contact details before publishing if desired.
