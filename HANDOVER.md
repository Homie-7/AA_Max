# Handover: bearinmind patches + Android Auto (AA_Max)

Goal: one Morphe patch bundle for Google Maps 26.44.06.1006895251 that keeps all of
bearinmindcat's Maps features and adds working Android Auto. Owner drives a 2025 Haval H6
(car, not motorcycle), Oppo Find X9 Ultra phone.

## State of this repo
- Base: bearinmindcat/morphe-patches v2.10.0 (commit 1df2542), unchanged.
- Added from shuili1013/Maps-AA-AnyMode-Morphe v1.0.3 (GPLv3, includes Dutch App Patches code):
  - `patches/src/main/kotlin/org/ungoogled/patches/maps/car/` (5 patches; only package lines changed)
    - NativeNavigationPatch.kt: "Native navigation for Gearslip and Android Auto" (default on)
    - SkipPrivateCarConnection.kt (on)
    - AllowAnyModeOnAndroidAuto.kt (on)
    - HideLegacyProjectionEntry.kt: "Android Auto template-only app" (on)
    - AlwaysTwoWheelerOnAndroidAuto.kt (OFF; with it on, Android Auto refuses driving routes)
  - `extensions/maps-gearslip/` (plain Java, Android SDK imports only; tests dropped)
- `.github/workflows/build.yml` builds the .mpp and uploads it as artifact `patches-mpp`.
  bearinmindcat's release workflows were removed.
- `patches-bundle.json` was deleted: it pointed at bearinmindcat's own release, so adding this
  repo to Morphe as a remote source would have installed the bundle WITHOUT Android Auto.
- Version: 2.10.0-aa.1. Bundle name: "bearinmind patches + Android Auto".

## Build status (2026-11-19)
`./gradlew :patches:buildAndroid` passes in GitHub Actions (run 44437418699, artifact
`patches-mpp`, about 10.6 MB). Confirmed by that build:
- The Morphe settings plugin auto-includes `extensions/maps-gearslip`; it compiles and dexes.
- The car patches compile against plugin 2.0.0 / patcher 2.0.0 with no source changes.
- The only failure was the Gradle daemon running out of its default 512 MiB heap in
  `:patches:buildAndroid`; `org.gradle.jvmargs` in gradle.properties now sets 4g.
Download the .mpp from the run page in a browser (Actions > run > Artifacts).

## NOT verified
Patch application against the real APKM, and anything on a phone or head unit.

## Audit findings so far
- No class overlap: no bearinmindcat Maps patch names the classes the car patches edit
  (jbz, nnu, lvg, tqb, ryt, beop, trl, pxt, trq, siy, btxk). Fingerprint-based patches could
  still hit the same methods; only a real patch run proves it.
- bearinmindcat's base patches are equal to or newer than the AA project's copies:
  ChangePackageName, RestoreMapData, gms Fingerprints, StockPackageLookup, MicrogLocationDialog
  identical; BypassPlayServices and MicrogSupport are supersets (Cronet fallback, push routing).
- "Remove permissions" drops `com.google.android.gms.permission.CAR_SPEED`. The native
  navigation patch already makes Maps treat car speed as optional, so this should be fine.
- "Remove telemetry" removes only Primes and federated-learning components, no car services.
- `ProjectionRenderer` reflects into `org.ungoogled.ui.ExternalTelemetry`, which exists only in
  Dutch App Patches. Absence is caught (ClassNotFoundException), so Dutch road-warning
  overlays on the car screen are simply missing.
- `HostPolicy` accepts only Gearslip/DriveDeck or a Google-signed Android Auto as the car host.

## Open questions to settle on device
- microG: the AA author says stock MicroG-RE cannot sign Maps in or give it location and
  ships a fork (shuili1013/MicroG-RE v7.3.0-maps-fix). bearinmindcat's newer microG patch and
  "Location provider toggle" may make that unnecessary. Test order: (a) no microG
  ("Ungoogled Maps", Android location), (b) "Add microG-RE support" with stock MicroG-RE,
  (c) the fork only if (b) fails.
- Install source: Android Auto lists Car App Library apps only if they look Play-installed.
  The AA author needed KingInstaller Classic with the single patched APK;
  `adb install -i com.android.vending` did not work for them. Developer "Unknown sources"
  does not cover CAL apps.
- Known gap inherited from the AA project: no next-turn info in Android Auto's side rail
  (NativeGuidance's reflection targets were renamed in 26.44). Fixing it needs the decompiled APK.
- Untested with the car session: Black theme, Power saving mode, Zoom controls in navigation,
  120 Hz. If the car screen misbehaves, disable these first.

## Next steps
1. DONE: Actions build is green; get the .mpp from the latest run's `patches-mpp` artifact.
2. Patch the 26.44.06 APKM in Morphe with this bundle (disable other Maps sources). Every car
   patch throws a PatchException with a specific message if Maps' code does not match.
3. Phone check first: map tiles, search, routing, location, each bearinmindcat feature in use.
4. Install via KingInstaller Classic, connect to the Haval while parked.
5. If Maps is missing or crashes on the head unit: `adb logcat` filtered on
   `CAR.VALIDATOR`, `PlayGearheadService`, `AndroidRuntime`, `patches.maps.gearslip`.
6. Once it works: add a release step to the workflow and a correct `patches-bundle.json`
   so the repo works as a Morphe remote source.

Upstreams: https://github.com/bearinmindcat/morphe-patches ,
https://github.com/shuili1013/Maps-AA-AnyMode-Morphe (its README has the full install guide,
findings.md and handoff*.md have the head-unit investigation).

## Owner's target configuration (decided 2026-11-19)
Signed in to his Google account (lots of saved places), ads and unnecessary telemetry blocked.
- ON: "Add microG-RE support" (builds microG Maps, package `org.ungoogled.android.apps.maps.microg`),
  "Hide ads and clutter", "Remove telemetry", plus the four default-on car patches.
- With microG support selected, bearinmindcat already keeps what account features need:
  "Remove telemetry" leaves Firebase Installations and Location History alone (Timeline, sync,
  push keep working), and "Remove permissions" and "Trim account menu" are left out.
- This makes the microG question the first thing to test, not the last: try stock MicroG-RE
  first; if sign-in or location fails, use shuili1013/MicroG-RE v7.3.0-maps-fix (different
  signing key, so stock MicroG-RE must be uninstalled first).
- In KingInstaller, pick the `.microg` package, not stock Maps.
