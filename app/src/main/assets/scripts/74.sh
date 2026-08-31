#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; LIST="$(unzip -l "$APK" 2>/dev/null)"; TMP="${TMPDIR:-$(dirname "$APK")}/msa72_aapt_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP"
echo '=== AAPT / AAPT2 ==='; echo "$LIST"|grep -Ei '(^|/)(aapt2?|.*_aapt2?)( |$)'|awk '{print $NF}'|head -40; printf 'AAPT refs: '; grep -Fic aapt "$TMP"; printf 'AAPT2 refs: '; grep -Fic aapt2 "$TMP"; rm -f "$TMP"
