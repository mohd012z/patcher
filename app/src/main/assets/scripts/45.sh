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

TMP="${TMPBASE}/msa72_ev_$$.txt"; NAT="${TMPBASE}/msa72_nat_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP" 2>/dev/null
unzip -p "$APK" 'lib/*/*.so' 2>/dev/null | strings > "$NAT" 2>/dev/null
LIST="$(unzip -l "$APK" 2>/dev/null)"
score(){ name="$1"; dex="$2"; asset="$3"; nat="$4"; d=0;a=0;n=0; grep -qiE "$dex" "$TMP"&&d=1; echo "$LIST"|grep -qiE "$asset"&&a=1; grep -qiE "$nat" "$NAT"&&n=1; total=$((d+a+n)); [ $total -ge 3 ]&&lvl=STRONG||{ [ $total -eq 2 ]&&lvl=MEDIUM||{ [ $total -eq 1 ]&&lvl=WEAK||lvl=NONE; }; }; echo "$name : $lvl [DEX=$d ASSET=$a NATIVE=$n]"; }
echo "=== Evidence Correlation ==="
score Xposed 'XposedBridge|IXposedHook' 'xposed_init|xposed' 'XposedBridge|xposed'
score LSPlant 'LSPlant|org/lsposed/lsplant' 'lsplant' 'lsplant::|LSPlant|LSPHooker'
score Magisk/Zygisk 'Magisk|Zygisk' 'magisk|zygisk' 'magisk|zygisk'
score Decompiler 'jadx|dexlib2|baksmali' 'jadx|smali' 'jadx|dexlib'
score Protection 'mtprotect|jiagu|pairip|secneo' 'protect|jiagu|pairip|secneo' 'mtprotect|jiagu|pairip|secneo'
rm -f "$TMP" "$NAT"
