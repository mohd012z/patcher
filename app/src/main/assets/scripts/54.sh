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

TMP="${TMPBASE}/msa72_sig_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$TMP" 2>/dev/null
echo "=== Signature Surface ==="; for p in 'ApkSignerEngine' 'APK Signature Scheme v2' 'APK Signature Scheme v3' 'SigningCertificateLineage' 'SourceStamp'; do c=$(grep -Fic "$p" "$TMP"); [ $c -gt 0 ]&&echo "$p : PRESENT ($c)"; done
echo "Embedded signing-related filenames:"; unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Ei '\.(pk8|pem|keystore|jks|sbt)$|META-INF/.+\.(RSA|DSA|EC|SF)$'|head -120; echo "Note: private-key contents are not extracted."; rm -f "$TMP"
