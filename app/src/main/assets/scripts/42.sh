#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
TMP="${TMPDIR:-/data/local/tmp}/mp7dom_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings | grep -Eo '([A-Za-z0-9-]+\.)+[A-Za-z]{2,}' | tr 'A-Z' 'a-z' | sort -u > "$TMP"
echo "=== Domain Intelligence ==="
echo "-- Advertising candidates --"
grep -Ei 'doubleclick|googlesyndication|applovin|pangle|vungle|unityads|inmobi|moloco|mbridge|mintegral' "$TMP" | head -80
echo "-- Analytics / attribution candidates --"
grep -Ei 'appsflyer|posthog|umeng|firebase|analytics|crashlytics|adjust' "$TMP" | head -80
echo "-- Other / application domains (sample) --"
grep -Eiv 'doubleclick|googlesyndication|applovin|pangle|vungle|unityads|inmobi|moloco|mbridge|mintegral|appsflyer|posthog|umeng|firebase|analytics|crashlytics|adjust' "$TMP" | head -80
rm -f "$TMP"
