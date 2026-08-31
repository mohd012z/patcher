#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; T="${TMPDIR:-$(dirname "$APK")}/msa72_mem_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$T"; echo '=== MEMORY CAPABILITY ==='; for p in memory_editor searchNumber searchAddress searchPointer dumpMemory copyMemory memory_range ptrace; do c=$(grep -Fic "$p" "$T"); [ "$c" -gt 0 ]&&printf '%-20s %s\n' "$p" "$c"; done; rm -f "$T"
