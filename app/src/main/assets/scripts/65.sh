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

TMP="${TMPBASE}/msa72_mag_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP"; echo "=== Magisk / Zygisk ==="; grep -Ei '/data/adb/(modules|magisk)|/sbin/\.magisk|zygisk|magisk\.db|magisk module' "$TMP"|sort -u|head -120; rm -f "$TMP"
