#!/system/bin/sh
main_func(){ F="$@"; B="${0%/*}/bin/busybox"; base="${F##*/}"; dir="${F%$base}"; [ -n "$dir" ] || dir="./"; name="${base%.*}"; ext="${base##*.}"; OUT="${dir}${name}_MSAPatcherBackup.${ext}"; $B cp -f "$F" "$OUT" || ab_ort "Backup failed."; setspan_green "\n  Direct APK snapshot created."; setspan_blue "  $OUT\n"; }
[ -f "${0%/*}/bin/utils" ] && . "${0%/*}/bin/utils" || { setspan_red(){ echo "$@"; }; setspan_green(){ echo "$@"; }; setspan_blue(){ echo "$@"; }; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }; [ -f "$@" ] || ab_ort "Select an APK file."; main_func "$@"; exit
