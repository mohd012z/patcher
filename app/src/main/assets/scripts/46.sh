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

LIST="$(unzip -l "$APK" 2>/dev/null)"; TMP="${TMPBASE}/msa72_conf_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP" 2>/dev/null
coverage=98; behavior=82
SON=$(echo "$LIST"|grep -Ec ' lib/[^ ]+\.so$'); DEXN=$(echo "$LIST"|grep -Ec ' classes([0-9]+)?\.dex$')
[ "$SON" -gt 10 ] && behavior=$((behavior-10)); [ "$SON" -gt 30 ] && behavior=$((behavior-8))
grep -qiE 'DexClassLoader|PathClassLoader|InMemoryDexClassLoader' "$TMP" && behavior=$((behavior-8))
echo "$LIST"|grep -qiE 'jiagu|pairip|secneo|shell|stub|mtprotect' && { coverage=$((coverage-8)); behavior=$((behavior-8)); }
echo "$LIST"|grep -q 'libflutter\.so' && { coverage=$((coverage-5)); behavior=$((behavior-7)); }
[ "$DEXN" -gt 30 ] && coverage=$((coverage-3)); [ "$coverage" -lt 0 ]&&coverage=0; [ "$behavior" -lt 0 ]&&behavior=0
echo "=== Dual Confidence ==="; echo "Analysis Coverage: $coverage%"; echo "Behaviour Confidence: $behavior%"; echo "DEX: $DEXN  Native: $SON"; echo "Coverage = how much static surface is visible."; echo "Behaviour = confidence about what actually executes at runtime."
rm -f "$TMP"
