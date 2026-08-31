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

echo "=== Embedded Web Application ==="; unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Ei '^assets/.+\.(html|js|css)$'|head -180; echo "HTML: $(unzip -l "$APK"|awk '{print $NF}'|grep -Eic '^assets/.+\.html$') JS: $(unzip -l "$APK"|awk '{print $NF}'|grep -Eic '^assets/.+\.js$') CSS: $(unzip -l "$APK"|awk '{print $NF}'|grep -Eic '^assets/.+\.css$')"
