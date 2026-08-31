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

TMP="${TMPBASE}/msa72_dec_$$.txt"; unzip -p "$APK" '*.dex' 2>/dev/null|strings > "$TMP" 2>/dev/null
echo "=== Decompiler Capability ==="
check(){ c=$(grep -Eic "$2" "$TMP"); [ "$c" -gt 0 ]&&echo "$1 : PRESENT ($c refs)"||echo "$1 : no strong match"; }
check JADX 'jadx/core|jadx/api|JadxDecompiler'; check dexlib2 'org/jf/dexlib2|dexlib2'; check smali 'org/jf/smali|smali'; check baksmali 'org/jf/baksmali|baksmali'; check DexMerger 'com/android/dx/merge/DexMerger|DexMerger'; check SourceExport 'ExportGradleProject|Sources_Export'
rm -f "$TMP"
