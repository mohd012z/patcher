#!/system/bin/sh
APK="$1"; [ -f "$APK" ] || exit 1; T="${TMPDIR:-$(dirname "$APK")}/msa72_lua_$$"; unzip -p "$APK" 'classes*.dex' 2>/dev/null|strings > "$T"; echo '=== SCRIPTING ENGINE ==='; echo "Lua refs: $(grep -Eic '(^|[^a-z])lua([^a-z]|$)' "$T")"; echo "LuaJ refs: $(grep -Eic 'luaj/' "$T")"; echo "Script API refs: $(grep -Eic 'Script\$|\.lua$|script generated' "$T")"; rm -f "$T"
