#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; T="${TMPDIR:-$(dirname "$APK")}/msa72_daemon_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$T"; echo '=== DAEMON / SERVICE ARCHITECTURE ==='; grep -Ei 'daemon(_root|_vroot)?|BootstrapService|Service.*memory|foreground.?service' "$T"|sort -u|head -100; rm -f "$T"
