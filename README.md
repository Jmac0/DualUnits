# Dual Units – Karoo extension

Four data fields that show metric at full size. **Tap a field to switch it to imperial** (shown in blue), and tap again to switch back. Each field remembers its own setting.

| Field      | Default | Tap      |
|------------|---------|----------|
| Speed      | km/h    | mph      |
| Avg Speed  | km/h    | mph      |
| Distance   | km      | mi       |
| Dist to Go | km      | mi       |

"Dist to Go" shows `--` when you are not navigating a route.

## Build

1. Install **Android Studio** (it includes the Android SDK and a compatible JDK).
2. Open this `DualUnits` folder in Android Studio and let Gradle sync.
3. **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Or from the terminal (the Android SDK is installed via Homebrew on this Mac):
```
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
```

No GitHub token is needed: Hammerhead's `karoo-ext` SDK (v1.1.9, Apache-2.0) is included in `karoo-ext/`.

## Install on the Karoo 3 (USB)

1. On the Karoo, open **Settings → About** and tap the build number repeatedly to turn on developer options. Then turn on **USB debugging**.
2. Plug the Karoo into the Mac and run:
   ```
   /opt/homebrew/share/android-commandlinetools/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   The Karoo will ask you to allow debugging the first time.

## Add the fields

On the Karoo, go to **Profiles → (your profile) → edit a page → tap a field**. The four fields are listed under **Dual Units**.
