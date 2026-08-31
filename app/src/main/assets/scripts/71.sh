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

D="${TMPBASE}/msa72_sdkd_$$.txt"; N="${TMPBASE}/msa72_sdkn_$$.txt"; DOM="${TMPBASE}/msa72_sdkdom_$$.txt"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$D"; unzip -l "$APK" 2>/dev/null|awk '{print $NF}' > "$N"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings|grep -Eo '([A-Za-z0-9-]+\.)+[A-Za-z]{2,}'|tr A-Z a-z > "$DOM"
score(){ name="$1"; dp="$2"; np="$3"; xp="$4"; a=0;b=0;c=0; grep -qiE "$dp" "$D"&&a=1; grep -qiE "$np" "$N"&&b=1; grep -qiE "$xp" "$DOM"&&c=1; s=$((a+b+c)); [ $s -eq 3 ]&&lvl='STRONG 95%+'||{ [ $s -eq 2 ]&&lvl='MEDIUM 70-90%'||{ [ $s -eq 1 ]&&lvl='WEAK <50%'||lvl='NONE'; }; }; echo "$name : $lvl [DEX=$a Native=$b Domain=$c]"; }
echo "=== SDK 3-Layer Correlator ==="; score Umeng 'com/umeng|umeng' 'umeng' 'umeng\.com'; score GoogleAds 'google/android/gms/ads' 'google.*ads' 'googlesyndication|doubleclick'; score AppLovin 'applovin' 'applovin' 'applovin\.com'; score Pangle 'bytedance/sdk/openadsdk' 'pangle|bytedance' 'pangle|bytedance'; score AppsFlyer 'appsflyer' 'appsflyer' 'appsflyer\.com'; rm -f "$D" "$N" "$DOM"
