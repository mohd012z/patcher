#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
TMPBASE="${TMPDIR:-}"
if [ -z "$TMPBASE" ] || [ ! -d "$TMPBASE" ] || [ ! -w "$TMPBASE" ]; then
  TMPBASE=""
  for d in "$(dirname "$APK")" "${HOME:-}" /sdcard/Download /data/local/tmp; do
    [ -n "$d" ] && [ -d "$d" ] && [ -w "$d" ] && { TMPBASE="$d"; break; }
  done
fi
[ -n "$TMPBASE" ] || { echo "No writable temporary directory available."; exit 1; }

D="${TMPBASE}/msa72_dj_$$.txt"; N="${TMPBASE}/msa72_nj_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$D"; unzip -p "$APK" 'lib/*/*.so' 2>/dev/null|strings > "$N"
echo "=== DEX ↔ JNI Correlation ==="; echo "DEX loadLibrary refs: $(grep -Eic 'loadLibrary|System;->load| native ' "$D")"; echo "Native JNI_OnLoad: $(grep -Fc 'JNI_OnLoad' "$N")"; echo "Java_* JNI symbols: $(grep -Ec '^Java_' "$N")"; rm -f "$D" "$N"
