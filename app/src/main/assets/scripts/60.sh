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

echo "=== Nested APK Scanner ==="; for n in $(unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -E '\.apk$'|head -20); do echo "-- $n"; TMP="${TMPBASE}/msa72_nested_$$.apk"; unzip -p "$APK" "$n" > "$TMP" 2>/dev/null; echo "size: $(wc -c < "$TMP") bytes"; echo "entries: $(unzip -l "$TMP" 2>/dev/null|tail -1|awk '{print $2}')"; echo "dex: $(unzip -l "$TMP" 2>/dev/null|grep -Ec 'classes([0-9]+)?\.dex$')"; rm -f "$TMP"; done
