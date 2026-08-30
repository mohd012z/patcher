#!/usr/bin/env sh
set -eu
test -f app/src/main/AndroidManifest.xml
grep -q 'android:label="MSAPatcher"' app/src/main/AndroidManifest.xml
grep -q 'versionCode = 80' app/build.gradle.kts
grep -q 'versionName = "8.0"' app/build.gradle.kts
test -f app/src/main/assets/scripts/index.json
echo "MSAPatcher V8 source identity: PASS"
