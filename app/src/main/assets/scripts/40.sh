#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
TMP="${TMPDIR:-/data/local/tmp}/mp7sdk_$$.txt"
unzip -p "$APK" 'classes*.dex' 2>/dev/null | strings > "$TMP" 2>/dev/null
echo "=== SDK Fingerprint Matrix ==="
check(){ name="$1"; pat="$2"; c=$(grep -Eic "$pat" "$TMP" 2>/dev/null); [ "$c" -gt 0 ] && echo "$name : detected ($c string refs)" || echo "$name : no strong match"; }
check "Google Mobile Ads" 'google/android/gms/ads|googlesyndication|doubleclick'
check "AppLovin MAX" 'applovin|mediation/max'
check "Pangle" 'bytedance/sdk/openadsdk|pangle'
check "Meta Audience Network" 'facebook/ads|AudienceNetwork'
check "Mintegral/MBridge" 'mbridge/msdk|mintegral'
check "Moloco" 'moloco'
check "Unity Ads" 'unity3d/ads|unityads'
check "Vungle/Liftoff" 'vungle'
check "AppsFlyer" 'appsflyer'
check "Firebase Analytics" 'firebase/analytics|google/firebase/analytics'
check "Umeng" 'umeng|umonitor'
rm -f "$TMP"
echo
echo "Detection is evidence-based; protected/dynamic code can reduce visibility."
