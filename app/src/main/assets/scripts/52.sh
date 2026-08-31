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

LIST="$(unzip -l "$APK" 2>/dev/null)"; echo "=== Flutter/AOT Detector ==="
echo "$LIST"|grep -E 'lib/[^/]+/(libflutter|libapp)\.so$' || true
echo "$LIST"|grep -q 'libflutter\.so' && echo "Flutter engine: CONFIRMED" || echo "Flutter engine: not detected"
echo "$LIST"|grep -q 'libapp\.so' && echo "Dart AOT app logic: PRESENT - static visibility reduced" || true
