#!/system/bin/sh
F="$1"
echo "MSAPatcher 7.3 • GUIDED DEEP ANALYSIS"
echo "====================================="
[ -f "$F" ] || { echo "Select an APK file first."; exit 1; }
echo "Target: $(basename "$F")"
echo
echo "Recommended flow:"
echo "01 Overview / Architecture"
echo "02 APK Structure"
echo "03 DEX & Code"
echo "04 Native & JNI"
echo "05 Resources & Build"
echo "06 SDK & Network"
echo "07 Root & Virtualization"
echo "08 Hook & Runtime"
echo "09 Signing & Integrity"
echo "10 Memory & Process"
echo "11 Protection"
echo "12 Evidence & Report"
echo
echo "Only run specialized categories relevant to the detected architecture."
