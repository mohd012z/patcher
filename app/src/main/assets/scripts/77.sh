#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; echo '=== SIGNATURE-HOOK PAYLOAD INVENTORY ==='; unzip -l "$APK" 2>/dev/null|awk '{print $NF}'|grep -Ei 'signature.*(killer|hook)|killer.*\.(dex|smali|so)$|hookapplication.*smali'|head -80; echo 'Inventory only; payloads are not executed.'
