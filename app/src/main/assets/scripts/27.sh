#!/system/bin/sh
setspan_green(){ echo "$@"; }
setspan_blue(){ echo "$@"; }
setspan_red(){ echo "$@"; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }
[ -n "$1" ] || ab_ort "No project directory passed!"
[ -d "$1" ] || ab_ort "Project directory does not exist: $1"
cd "$1" || ab_ort "Cannot enter project directory"
OUT="MSAPatcher_AdAudit.txt"
TMP=".mpatcher_ad_audit.tmp"
: > "$OUT"
{
 echo "MSAPatcher 6 - Ad SDK Audit"
 echo "Project: $(pwd)"
 echo "Generated: $(date 2>/dev/null)"
 echo ""
 echo "== SDK / Mediation Detection =="
} >> "$OUT"
scan(){
 label="$1"; pat="$2";
 c=$(grep -R -I -E -l "$pat" AndroidManifest.xml smali* res assets 2>/dev/null | wc -l | tr -d ' ')
 [ -z "$c" ] && c=0
 if [ "$c" -gt 0 ] 2>/dev/null; then printf "[FOUND] %-24s refs/files=%s\n" "$label" "$c" >> "$OUT"; else printf "[----]  %-24s refs/files=0\n" "$label" >> "$OUT"; fi
}
scan "Google Mobile Ads" "com/google/android/gms/ads|googleads|doubleclick|googlesyndication"
scan "AppLovin / MAX" "com/applovin|applovin|MaxInterstitial|MaxRewarded|MaxNative"
scan "Pangle / ByteDance" "com/bytedance/sdk/openadsdk|pangle|TTReward|TTFullScreen"
scan "Meta Audience Network" "com/facebook/ads|AudienceNetworkContentProvider"
scan "Mintegral / MBridge" "com/mbridge/msdk|mintegral|mbridge"
scan "Moloco" "com/moloco|dsp-api\.moloco"
scan "Unity Ads" "com/unity3d/ads|unityads"
scan "Vungle / Liftoff" "com/vungle|vungle\.com"
scan "InMobi" "com/inmobi|inmobi\.com"
scan "ironSource / LevelPlay" "com/ironsource|ironsource"
scan "Chartboost" "com/chartboost|chartboost"
scan "Bigo Ads" "com/bigossp|bigo.*ads"
scan "AppsFlyer" "com/appsflyer|appsflyer"
scan "Firebase Analytics" "com/google/firebase/analytics|firebase_analytics"
{
 echo ""
 echo "== Ad Format Indicators =="
} >> "$OUT"
scan_format(){ label="$1"; pat="$2"; c=$(grep -R -I -E "$pat" smali* res AndroidManifest.xml 2>/dev/null | wc -l | tr -d ' '); [ -z "$c" ] && c=0; printf "%-22s %s refs\n" "$label" "$c" >> "$OUT"; }
scan_format "Interstitial" "Interstitial|interstitial"
scan_format "Rewarded" "Rewarded|rewarded_video|AD_REWARD"
scan_format "App Open / Splash" "AppOpen|app_open|OpenScreen|SplashAd"
scan_format "Native" "NativeAd|native_ad|NativeAdView"
scan_format "Banner" "BannerAd|banner_ad|AdView"
{
 echo ""
 echo "== Notes =="
 echo "FOUND means code/resources matching the SDK family exist in the decompiled project."
 echo "It does not prove that the SDK is enabled at runtime. Remote configuration may control activation."
 echo "This module is read-only and makes no project changes."
} >> "$OUT"
setspan_green "\n  Ad SDK audit complete."
setspan_blue "  Report: $(pwd)/$OUT\n"
cat "$OUT"
exit 0
