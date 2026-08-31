#!/system/bin/sh
F="$1"
echo "MSAPatcher 7.3 • ANALYSIS FLOW"
echo "=============================="
[ -f "$F" ] && echo "Target: $(basename "$F")"
echo
echo "PRECHECK → ARCHITECTURE → CORE → SPECIALIZED → CORRELATE → CONFIDENCE → REPORT"
echo
echo "Confidence model:"
echo "• Analysis Coverage = how much of the APK was statically inspected"
echo "• Behaviour Confidence = how strongly static evidence supports runtime behaviour"
echo
echo "Evidence levels: CONFIRMED / STRONG / MEDIUM / WEAK / REJECTED"
