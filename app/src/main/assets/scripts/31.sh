#!/system/bin/sh
main_func(){
F="$@"; util_dir="${0%/*}/bin"; BB="${util_dir}/busybox"; base="${F##*/}"; dir="${F%$base}"; [ -n "$dir" ] || dir="./"; name="${base%.*}"; OUT="${dir}${name}_MSAPatcher_DirectAudit.txt";
setspan_green "\n  Direct APK Analyzer...\n";
[ -f "$F" ] || ab_ort "APK file not found: $F";
MAGIC="$($BB hexdump -n 4 -C "$F" | $BB head -n1 | $BB cut -f3,4,5,6 -d ' ')"; [ "$MAGIC" = "50 4b 03 04" ] || ab_ort "Selected file is not a ZIP/APK container.";
LIST="$($BB unzip -qql "$F" 2>/dev/null | $BB sed 's|.* ||g')";
DEXN="$(echo "$LIST" | $BB grep -E '^classes([0-9]*)?\.dex$' | $BB wc -l | $BB tr -d ' ')";
LIBN="$(echo "$LIST" | $BB grep -E '^lib/.+\.so$' | $BB wc -l | $BB tr -d ' ')";
SIG="$(echo "$LIST" | $BB grep -E '^META-INF/.+\.(RSA|DSA|EC|SF)$' | $BB wc -l | $BB tr -d ' ')";
ABIS="$(echo "$LIST" | $BB grep '^lib/' | $BB cut -d/ -f2 | $BB sort -u | $BB tr '\n' ' ')";
{
echo "MSAPatcher 6.1 - Direct APK Audit"; echo "APK: $base"; echo "Generated: $(date 2>/dev/null)"; echo; echo "== Structure =="; echo "DEX files: $DEXN"; echo "Native .so files: $LIBN"; echo "ABIs: ${ABIS:-none}"; echo "Legacy META-INF signature entries: $SIG"; echo; echo "== SDK markers (direct DEX scan) ==";
} > "$OUT";
scan(){ label="$1"; pat="$2"; hits=0; for d in $(echo "$LIST" | $BB grep -E '^classes([0-9]*)?\.dex$'); do c="$($BB unzip -p "$F" "$d" 2>/dev/null | $BB grep -a -E -o "$pat" 2>/dev/null | $BB head -n 50 | $BB wc -l | $BB tr -d ' ')"; [ -n "$c" ] && hits=$((hits+c)); done; if [ "$hits" -gt 0 ]; then printf "[FOUND] %-24s markers=%s\n" "$label" "$hits" >> "$OUT"; else printf "[----]  %-24s markers=0\n" "$label" >> "$OUT"; fi; }
scan "Google Mobile Ads" 'com/google/android/gms/ads|googleads|doubleclick|googlesyndication';
scan "AppLovin / MAX" 'com/applovin|applovin|MaxInterstitial|MaxRewarded';
scan "Pangle / ByteDance" 'com/bytedance/sdk/openadsdk|pangle|TTReward|TTFullScreen';
scan "Meta Audience Network" 'com/facebook/ads|AudienceNetwork';
scan "Mintegral / MBridge" 'com/mbridge/msdk|mintegral|mbridge';
scan "Moloco" 'com/moloco|dsp-api\.moloco';
scan "Unity Ads" 'com/unity3d/ads|unityads';
scan "Vungle / Liftoff" 'com/vungle|vungle\.com';
scan "InMobi" 'com/inmobi|inmobi\.com';
scan "ironSource / LevelPlay" 'com/ironsource|ironsource';
scan "AppsFlyer" 'com/appsflyer|appsflyer';
scan "Firebase Analytics" 'com/google/firebase/analytics|firebase_analytics';
{
echo; echo "== Ad format markers ==";
} >> "$OUT";
scan "Interstitial" 'Interstitial|interstitial'; scan "Rewarded" 'Rewarded|rewarded_video|AD_REWARD'; scan "App Open / Splash" 'AppOpen|app_open|OpenScreen|SplashAd'; scan "Native" 'NativeAd|native_ad'; scan "Banner" 'BannerAd|banner_ad|AdView';
{
echo; echo "== Embedded web domains (sample) ==";
for d in $(echo "$LIST" | $BB grep -E '^classes([0-9]*)?\.dex$'); do $BB unzip -p "$F" "$d" 2>/dev/null | $BB grep -a -E -o '([A-Za-z0-9-]+\.)+[A-Za-z]{2,}' 2>/dev/null; done | $BB sort -u | $BB head -n 80;
echo; echo "== Notes =="; echo "This analysis reads APK entries directly and does not create a decompiled project."; echo "SDK markers indicate packaged capability, not necessarily runtime activation.";
} >> "$OUT";
setspan_green "  Analysis complete."; setspan_blue "  Report: $OUT\n"; cat "$OUT";
}
[ -f "${0%/*}/bin/utils" ] && . "${0%/*}/bin/utils" || { setspan_red(){ echo "$@"; }; setspan_green(){ echo "$@"; }; setspan_blue(){ echo "$@"; }; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }
[ -n "$@" ] || ab_ort "No APK passed."; [ -f "$@" ] || ab_ort "Select an APK file."; main_func "$@"; exit
