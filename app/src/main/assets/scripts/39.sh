#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
echo "=== MSAPatcher 7 Analysis Confidence ==="
echo "File: $(basename "$APK")"
echo "Size: $(du -h "$APK" | awk '{print $1}')"
score=100
flags=""
# Stream strings from archive entries; avoid full extraction.
LIST="$(unzip -l "$APK" 2>/dev/null)"
echo "$LIST" | grep -qiE 'libjiagu|\.jiagu|libjgdtc|libmtprotect|pairip' && { score=$((score-30)); flags="$flags
- Packer/protection indicators"; }
TMP="${TMPDIR:-/data/local/tmp}/mp7_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP" 2>/dev/null
grep -qiE 'DexClassLoader|PathClassLoader|loadDex|DexFile' "$TMP" && { score=$((score-15)); flags="$flags
- Dynamic DEX/class loading"; }
grep -qiE 'System\.loadLibrary|Runtime;->load|native ' "$TMP" && { score=$((score-10)); flags="$flags
- Native/JNI references"; }
DEXN="$(echo "$LIST" | grep -E ' classes[0-9]*\.dex$| classes\.dex$' | wc -l)"
[ "$DEXN" -gt 4 ] && score=$((score-5))
rm -f "$TMP"
[ "$score" -lt 0 ] && score=0
[ "$score" -ge 80 ] && level="HIGH" || { [ "$score" -ge 55 ] && level="MEDIUM" || level="LOW"; }
echo "DEX files: $DEXN"
echo "Static-analysis confidence: $score/100 ($level)"
[ -n "$flags" ] && printf "Confidence reducers:%b
" "$flags"
echo "Note: LOW confidence means static results may be incomplete."
