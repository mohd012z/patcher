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

TMP="${TMPBASE}/msa72_jni_$$.txt"; unzip -p "$APK" 'lib/*/*.so' 2>/dev/null|strings > "$TMP" 2>/dev/null
echo "=== JNI Signature Mapper ==="; grep -E '^Java_[A-Za-z0-9_]+|JNI_OnLoad|RegisterNatives|GetNativeFunction' "$TMP"|sort -u|head -160; echo "JNI evidence count: $(grep -Ec '^Java_|JNI_OnLoad|RegisterNatives' "$TMP")"; rm -f "$TMP"
