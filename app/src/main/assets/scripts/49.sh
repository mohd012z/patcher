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

LIST="$(unzip -l "$APK" 2>/dev/null)"; D=$(echo "$LIST"|grep -Ec ' classes([0-9]+)?\.dex$')
echo "=== Extreme Multidex ==="; echo "DEX count: $D"; echo "$LIST"|awk '/ classes([0-9]+)?\.dex$/{print $1,$NF}'
if [ "$D" -ge 50 ]; then echo "Level: EXTREME"; elif [ "$D" -ge 10 ]; then echo "Level: HIGH"; elif [ "$D" -ge 2 ]; then echo "Level: NORMAL MULTIDEX"; else echo "Level: SINGLE DEX"; fi
echo "$LIST"|grep -qiE 'shell|stub|jiagu|pairip|secneo' && echo "Concealment hint: PRESENT" || echo "Concealment hint: not obvious"
