#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings|grep -E '^/proc/|/proc/self/'|sort -u|head -80 | sed '1i=== PROCFS INTELLIGENCE ==='
