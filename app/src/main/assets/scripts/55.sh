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

TMP="${TMPBASE}/msa72_split_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP" 2>/dev/null
echo "=== Split APK Analyzer ==="; grep -Ei 'split apk|split_apk|splitInstall|base\.apk|split_config|Merge Split APK' "$TMP"|sort -u|head -80; rm -f "$TMP"
