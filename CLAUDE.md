# Dual Units – Karoo 3 extension

Hammerhead Karoo 3 extension (Android, Kotlin) for a rider moving from mph to km/h.
Graphical data fields show the value in metric at the Karoo's standard size; **tapping a
field toggles it to imperial** (shown in blue) and back. Each field remembers its setting.

Fields (typeIds): `speed`, `avg-speed`, `lap-avg-speed`, `distance`, `distance-to-go`.

## Build & install

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home  # system Java 27 is too new for Gradle 8.7
./gradlew assembleDebug
ADB=/opt/homebrew/share/android-commandlinetools/platform-tools/adb
$ADB install -r app/build/outputs/apk/debug/app-debug.apk
$ADB exec-out screencap -p > /tmp/shot.png   # screenshot the Karoo
```

- Android SDK is installed via Homebrew at `/opt/homebrew/share/android-commandlinetools` (`local.properties` points there; not committed).
- Karoo 3 connects over USB with USB debugging on; `adb devices` shows model `k24`.
- After install the Karoo reconnects and restarts views ~6–12 s later. Screenshots taken sooner show the old version. In the page editor the user may need to close/reopen to see changes.
- Karoo logs its ViewConfig for each field: `adb logcat -d | grep "starting view for TYPE_EXT::dualunits"`.
- `adb shell uiautomator dump` gives real on-screen bounds of our views — useful for layout issues.

## Layout

- `app/src/main/kotlin/com/macproductions/dualunits/`
  - `DualUnitsExtension.kt` – the KarooExtension service; list of fields and which Karoo data type each reads.
  - `DualUnitDataType.kt` – generic field: streams a Karoo value, combines with tap state, renders via Glance.
  - `DualUnitView.kt` – drawing (see below).
  - `UnitToggle.kt` – per-field km/mi state (SharedPreferences + StateFlow) and `ToggleReceiver` for taps.
  - `Units.kt` – conversions/formatting. Karoo gives speed in **m/s**, distance in **metres**.
- `app/src/main/res/xml/extension_info.xml` – field declarations (must match typeIds above).
- `karoo-ext/` – vendored copy of Hammerhead's karoo-ext SDK v1.1.9 (Apache 2.0) so no GitHub Packages token is needed. Don't edit it.

To add a field: add a `DualUnitDataType` in `DualUnitsExtension.kt`, a `<DataType>` in `extension_info.xml`, strings in `values/strings.xml`, and update the README table.

## Hard-won findings (verified on the device)

- **Taps work** on graphical fields on the ride screen (Glance `clickable(actionSendBroadcast(...))`). In the page editor taps select the field instead.
- **Don't render numbers with Glance `Text`** at the Karoo's `textSize`: Android font padding makes the line taller than the field and the digits get clipped. Numbers are drawn with `Paint` into ALPHA_8 bitmaps, **cropped tightly to the glyphs**, shown at true pixel size, and tinted with `ColorProvider(R.color.…)` so the Karoo resolves theme colours.
- The area under the Karoo's field title is **not** a fixed height (half-width 236×125 slot leaves only 56 px; full-width 478×160 leaves ~112 px), so don't assume a header height.
- Regular weight (`Typeface.DEFAULT`) matches the Karoo's built-in fields; bold looked heavier.
- Size target is `config.textSize * density`; only shrink if "value unit" is too wide.

## Preferences / status

- User only uses **dark mode** on the Karoo; light theme doesn't need testing.
- Speed, distance, avg speed verified on a ride. **Dist to Go not yet tested with a route** (shows `--` with no route).
- Public repo: https://github.com/Jmac0/DualUnits (MIT; SDK notice in `NOTICE`).
- Commit and push only when asked.

## Possible next steps

- Sign with a proper release key (currently the debug key) **before** sharing with anyone else — changing keys later forces reinstall.
- Add `io.hammerhead.karooext.MANIFEST_URL` meta-data + a hosted `manifest.json` (see `KarooAppManifest` in the SDK) so Karoo "Update" works; publish APKs on GitHub Releases.
- Submit to the community list github.com/timklge/awesome-karoo; ask in Hammerhead's Extensions Developers forum about the official Extensions library.
