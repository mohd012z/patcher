#!/system/bin/sh
APK="$1"
[ -f "$APK" ] || { echo "Select an APK file."; exit 1; }
echo "=== DEX / Native Inventory ==="
unzip -l "$APK" 2>/dev/null | awk '/classes([0-9]+)?\.dex$/ {print "DEX:",$NF,"compressed:",$1,"bytes"}'
echo
echo "Native ABIs:"
unzip -l "$APK" 2>/dev/null | awk '/ lib\/[^/]+\/[^/]+\.so$/ {split($NF,a,"/"); print a[2]}' | sort -u
echo
echo "Native libraries:"
unzip -l "$APK" 2>/dev/null | awk '/ lib\/[^/]+\/[^/]+\.so$/ {print $NF}' | sort
echo
echo "Protection hints:"
unzip -l "$APK" 2>/dev/null | grep -Ei 'jiagu|mtprotect|pairip|protect|shell|stub' | head -40
