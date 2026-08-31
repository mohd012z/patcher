#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; T="${TMPDIR:-$(dirname "$APK")}/msa72_proc_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$T"; echo '=== PROCESS CONTROL SURFACE ==='; grep -Ei 'process(Pause|Resume|Toggle|Kill)|isProcessPaused|process_vm_' "$T"|sort -u|head -80; rm -f "$T"
