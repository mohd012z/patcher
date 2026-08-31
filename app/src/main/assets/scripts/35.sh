#!/system/bin/sh
B="${0%/*}/bin/busybox"; F="$@"; [ -f "$F" ]||exit 1; base="${F##*/}"; dir="${F%$base}"; [ -n "$dir" ]||dir="./"; OUT="${dir}${base%.*}_MSAPatcher_Confidence.txt"
L="$($B unzip -qql "$F" 2>/dev/null|$B sed 's|.* ||g')"; tmp="${dir}.mpatcher_conf_$$"; for d in $(echo "$L"|$B grep -E '^classes([0-9]*)?\.dex$'); do $B unzip -p "$F" "$d" 2>/dev/null; done > "$tmp"
score(){ label="$1"; pkg="$2"; api="$3"; dom="$4"; s=0; ev=""; $B grep -aEq "$pkg" "$tmp"&&{ s=$((s+45)); ev="${ev} package"; }; $B grep -aEq "$api" "$tmp"&&{ s=$((s+30)); ev="${ev} api"; }; $B grep -aEq "$dom" "$tmp"&&{ s=$((s+15)); ev="${ev} domain"; }; echo "$L"|$B grep -Eiq "$pkg"&&{ s=$((s+10)); ev="${ev} resource"; }; [ "$s" -gt 100 ]&&s=100; lvl=LOW; [ "$s" -ge 40 ]&&lvl=MEDIUM; [ "$s" -ge 70 ]&&lvl=HIGH; printf "%-22s %3s%% %-6s evidence:%s\n" "$label" "$s" "$lvl" "${ev:- none}" >> "$OUT"; }
pack="$(echo "$L"|$B grep -Eic 'jiagu|protect|packer|shell|pairip')"; { echo "MSAPatcher 6.2 - Detection Confidence"; echo "APK: $base"; echo "Protection markers: $pack"; [ "$pack" -gt 0 ]&&echo "WARNING: protected/dynamic payloads can make static results incomplete."; echo; } > "$OUT"
score "Google Mobile Ads" 'google/android/gms/ads|googleads' 'InterstitialAd|RewardedAd|AdView|MobileAds' 'doubleclick|googlesyndication'
score "AppLovin MAX" 'applovin' 'MaxInterstitial|MaxRewarded|MediationService' 'applovin'
score "Pangle" 'bytedance/sdk/openadsdk|pangle' 'TTReward|TTFullScreen|OpenScreenAd' 'pangle'
score "Meta Audience" 'facebook/ads|AudienceNetwork' 'NativeAdLayout|InterstitialAd' 'facebook'
score "AppsFlyer" 'appsflyer' 'AppsFlyerLib|logEvent' 'appsflyer'
score "Umeng" 'umeng|umeng_analytics' 'MobclickAgent|UMConfigure' 'umeng'
$B rm -f "$tmp"; cat "$OUT"