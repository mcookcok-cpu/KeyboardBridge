# Keyboard Bridge v0.2

Bluetooth keyboard -> Android phone -> Wi-Fi -> Android/Google TV.

This version integrates the open-source Java Android TV Remote v2 implementation from `kunal52/AndroidTvRemote` at build time. The upstream project implements TLS pairing and remote sessions on ports 6467/6466. The app adds an Android UI, IP entry, pairing PIN dialog, and keyboard-to-TV key mapping.

## Build

Use Android Studio or run the included GitHub Actions workflow. The build machine needs Git because the workflow syncs the upstream remote library during Gradle build.

## Important

The upstream Java implementation is prerelease. Test first with the TV on the same LAN. Some TV firmware versions may require the newer Android TV Remote v2 implementation; if COOCAA rejects pairing, the next version should replace the engine with a newer protocol implementation.
