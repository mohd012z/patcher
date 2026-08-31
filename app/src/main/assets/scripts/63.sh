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

LIST="$(unzip -l "$APK" 2>/dev/null)"; TMP="${TMPBASE}/msa72_prot_$$.txt"; { unzip -p "$APK" 'classes*.dex' 2>/dev/null; unzip -p "$APK" 'lib/*/*.so' 2>/dev/null; }|strings > "$TMP" 2>/dev/null
echo "=== Protection Profiler ==="; check(){ c=$(grep -Eic "$2" "$TMP"); echo "$1 : $c evidence refs"; }; check Jiagu 'jiagu|libjgdtc'; check PairIP 'pairip'; check SecNeo 'secneo|bangcle'; check MTProtect 'mtprotect'; echo "$LIST"|grep -q 'libflutter\.so'&&echo 'Flutter AOT: PRESENT (not a packer by itself)'; rm -f "$TMP"
