# bearinmind patches + Android Auto (unofficial merge)

bearinmindcat/morphe-patches v2.10.0 with five Maps patches added from
shuili1013/Maps-AA-AnyMode-Morphe v1.0.3. Both target Google Maps 26.44.06.1006895251.

Added patches (on by default unless noted):
- Native navigation for Gearslip and Android Auto
- Skip private Android Auto car connection
- Allow any travel mode on Android Auto
- Android Auto template-only app
- Always two-wheeler on Android Auto (OFF by default: for motorcycle head units only;
  with it on, Android Auto refuses driving routes)

Status: source merge only. Not compiled, not run on a phone or head unit.

## Build
1. Create a new empty GitHub repo and push this tree to `main`.
2. Actions tab > "Build patch bundle" > Run workflow.
3. Download the `patches-mpp` artifact, unzip, add the `.mpp` to Morphe as a local source.
   Disable the original bearinmindcat source for Maps so patches are not listed twice.

## Install for Android Auto
Android Auto only lists the app if its install source looks like Play. Follow the
KingInstaller Classic steps in the Maps-AA-AnyMode-Morphe README with the single
patched APK exported from Morphe. Test while parked.
