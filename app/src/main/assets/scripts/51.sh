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

TMP="${TMPBASE}/msa72_dom_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings|grep -Eo '([A-Za-z0-9-]+\.)+[A-Za-z]{2,}'|tr 'A-Z' 'a-z'|sort|uniq -c|sort -nr > "$TMP"
echo "=== Library-Origin Domain Filter ==="; echo "-- Application/other candidates --"; grep -Eiv 'schemas\.android|w3\.org|apache\.org|slf4j\.org|developer\.android|github\.com|xml\.org|adobe\.com' "$TMP"|head -80; echo "-- Library/docs sample --"; grep -Ei 'schemas\.android|w3\.org|apache\.org|slf4j\.org|developer\.android|github\.com|xml\.org|adobe\.com' "$TMP"|head -40
rm -f "$TMP"
