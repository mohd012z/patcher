#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
TMP="${TMPDIR:-$(dirname "$APK")}/msa72_build_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP"
echo '=== MSAPatcher 7.2 | BUILD TOOLCHAIN ==='
for x in apktool aapt aapt2 zipalign apksigner dexlib2 smali baksmali jadx; do c=$(grep -Fic "$x" "$TMP"); [ "$c" -gt 0 ] && printf '%-12s %s refs\n' "$x" "$c"; done
unzip -l "$APK" 2>/dev/null | grep -E 'assets/.*/(aapt2?|zipalign|apksigner)( |$)' | awk '{print "Embedded binary: "$NF}' | head -30
rm -f "$TMP"
