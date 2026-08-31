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

echo "=== Embedded Payload Inventory ==="
unzip -l "$APK" 2>/dev/null | awk '{print $NF}' | grep -Ei '\.(apk|dex|jar|zip)$|patch\.txt$|instructions\.txt$' | head -200
printf '\nCounts:\n'
for e in apk dex jar zip; do c=$(unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Eic "\\.$e$"); echo "$e: $c"; done
