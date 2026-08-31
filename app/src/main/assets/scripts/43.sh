#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
BYTES=$(wc -c < "$APK")
MB=$((BYTES/1024/1024))
echo "=== Large APK Streaming Check ==="
echo "APK size: ${MB} MB"
echo "ZIP entries: $(unzip -l "$APK" 2>/dev/null | tail -1 | awk '{print $2}')"
echo "DEX count: $(unzip -l "$APK" 2>/dev/null | grep -Ec 'classes([0-9]+)?\.dex$')"
echo "Native .so count: $(unzip -l "$APK" 2>/dev/null | grep -Ec ' lib/.+\.so$')"
if [ "$MB" -lt 500 ]; then echo "Readiness: GOOD";
elif [ "$MB" -lt 1500 ]; then echo "Readiness: MODERATE - use selective/stream scans";
else echo "Readiness: HEAVY - avoid loading whole entries into RAM"; fi
echo "This module reads archive metadata and selected streams; it does not fully extract the APK."
