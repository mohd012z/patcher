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

TMP="${TMPBASE}/msa72_hook_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP" 2>/dev/null
echo "=== Hook Engine Analyzer ==="
unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Ei 'hook|xposed|lsplant|sandhook|yahfa|substrate|zygisk'|head -120
for t in 'XposedBridge' 'IXposedHook' 'LSPlant' 'SandHook' 'YAHFA' 'Substrate' 'Zygisk'; do c=$(grep -Fic "$t" "$TMP" 2>/dev/null); [ "$c" -gt 0 ]&&echo "$t : $c DEX refs"; done
rm -f "$TMP"
