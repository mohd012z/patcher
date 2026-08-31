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

echo "=== Embedded Signing-Key Inventory ==="; for e in pk8 pem sbt keystore jks; do c=$(unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Eic "\\.$e$"); echo "$e: $c"; done; unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Ei '\.(pk8|pem|sbt|keystore|jks)$'|head -120; echo 'Private key contents: NOT EXTRACTED'
