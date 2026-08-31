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

LIST="$(unzip -l "$APK" 2>/dev/null)"; TMP="${TMPBASE}/msa72_sum_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP"
DEXN=$(echo "$LIST"|grep -Ec ' classes([0-9]+)?\.dex$'); SON=$(echo "$LIST"|grep -Ec ' lib/[^ ]+\.so$'); ABI=$(echo "$LIST"|awk '/ lib\/[^/]+\/[^/]+\.so$/{split($NF,a,"/");print a[2]}'|sort -u|wc -l); NEST=$(echo "$LIST"|awk 'NF>=4 && $1 ~ /^[0-9]+$/ {print $NF}'|grep -Ec '\.apk$')
echo '=== MSAPatcher 7.2 Unified Intelligence ==='; echo "File: $(basename "$APK")"; echo "DEX: $DEXN | Native: $SON | ABI: $ABI | Nested APK: $NEST"; 
for item in 'XposedBridge:Xposed' 'LSPlant:LSPlant' 'Magisk:Magisk/Zygisk' 'jadx:JADX' 'dexlib2:dexlib2' 'DexClassLoader:Dynamic DEX' 'com/umeng:Umeng'; do p=${item%%:*}; n=${item#*:}; c=$(grep -Fic "$p" "$TMP"); [ $c -gt 0 ]&&echo "$n: PRESENT ($c refs)"; done
echo "Signing assets: $(echo "$LIST"|awk '{print $NF}'|grep -Eic '\.(pk8|pem|keystore|jks)$')"; echo "Web assets: $(echo "$LIST"|awk '{print $NF}'|grep -Eic '^assets/.+\.(html|js|css)$')"; echo 'Use Architecture + Evidence + Dual Confidence modules for interpretation.'; rm -f "$TMP"
