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

LIST="$(unzip -l "$APK" 2>/dev/null)"
TMP="${TMPBASE}/msa72_arch_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP" 2>/dev/null
DEXN=$(echo "$LIST"|grep -Ec ' classes([0-9]+)?\.dex$')
SON=$(echo "$LIST"|grep -Ec ' lib/[^ ]+\.so$')
flutter=0; hook=0; root=0; decomp=0
printf '%s\n' "$LIST"|grep -q 'libflutter\.so\|libapp\.so' && flutter=1
grep -qiE 'XposedBridge|LSPlant|DexClassLoader|IXposedHook' "$TMP" && hook=1
grep -qiE 'Magisk|/system/(x)?bin/su|busybox|Zygisk' "$TMP" && root=1
grep -qiE 'jadx|dexlib2|baksmali|DexMerger' "$TMP" && decomp=1
if [ "$flutter" -eq 1 ]; then cls="FLUTTER/NATIVE-AOT";
elif [ "$SON" -gt 15 ] && [ "$hook" -eq 1 ]; then cls="NATIVE-HOOK TOOLING";
elif [ "$root" -eq 1 ] && [ "$SON" -eq 0 ]; then cls="DEX/ROOT-FRAMEWORK TOOLING";
elif [ "$DEXN" -gt 20 ]; then cls="EXTREME-MULTIDEX";
elif [ "$decomp" -eq 1 ]; then cls="DEX/DECOMPILER TOOLING";
elif [ "$SON" -gt 0 ]; then cls="HYBRID DEX+NATIVE"; else cls="DEX-CENTRIC"; fi
echo "=== MSAPatcher 7.2 Architecture ==="
echo "Class: $cls"; echo "DEX: $DEXN"; echo "Native .so: $SON"; echo "Flutter/AOT hint: $flutter"; echo "Hook hint: $hook"; echo "Root-tooling hint: $root"; echo "Decompiler hint: $decomp"
rm -f "$TMP"
