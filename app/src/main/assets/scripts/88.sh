#!/system/bin/sh
F="$1"
echo "MSAPatcher 7.3 • QUICK APK TRIAGE"
echo "================================"
[ -f "$F" ] || { echo "Select an APK file first."; exit 1; }
echo "Target: $(basename "$F")"
echo "Size: $(wc -c < "$F" 2>/dev/null) bytes"
echo
echo "FLOW"
echo "1/5 Container precheck"
echo "2/5 Architecture hints"
echo "3/5 DEX / Native inventory"
echo "4/5 Evidence triage"
echo "5/5 Confidence summary"
echo
echo "Use Deep Analysis categories for evidence details."
