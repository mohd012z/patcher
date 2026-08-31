#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; T="${TMPDIR:-$(dirname "$APK")}/msa72_virt_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$T"; echo '=== VIRTUALIZATION ==='; grep -Ei 'com\.lody\.virtual|parallel_(intl|lite|pro)|virtual_space|vspace_root|daemon_vroot' "$T"|sort -u|head -80; rm -f "$T"
