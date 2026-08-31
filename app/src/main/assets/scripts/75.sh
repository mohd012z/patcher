#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; echo '=== EMBEDDED DEX ==='; unzip -l "$APK" 2>/dev/null|awk '$NF ~ /\.dex$/ {role="EMBEDDED"; if($NF ~ /^classes[0-9]*\.dex$/) role="PRIMARY"; printf "%-9s %10s bytes  %s\n",role,$1,$NF}'
