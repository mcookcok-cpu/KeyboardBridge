# Keyboard Bridge v0.3

Bluetooth Keyboard → Wi‑Fi → Android/Google TV.

## v0.3 fix

The previous build used the upstream Java library's desktop-oriented identity keystore. On Android this could fail with `Unable to create identity KeyStore`.

v0.3 replaces that part with an Android-safe PKCS12 identity store in the app's private files directory, uses one consistent key password, and automatically discards a stale/incompatible v0.2 keystore so a fresh identity can be generated.

The Android TV Remote engine is fetched from the upstream open-source project during the GitHub Actions build. The project uses pairing on port 6467 and remote communication on 6466.

## Build

Run GitHub Actions → `Build Keyboard Bridge APK`.
