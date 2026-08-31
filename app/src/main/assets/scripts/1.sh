#!/system/bin/sh
[ -f "${0%/*}/bin/utils" ] && { . "${0%/*}/bin/utils"; } || {
setspan_red() {
echo "$@";
}
setspan_green() {
echo "$@";
}
setspan_blue() {
echo "$@";
}
}
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";

setspan_green "
  Time:";
setspan_blue "    $(date +%H:%M:%S)";
setspan_green "
  OS core:";
setspan_blue "    $(uname -a)";
setspan_green "
  Util's:";
setspan_blue "$("${un_bb}" ls -R "${util_dir}" 2>/dev/null|\
"${un_bb}" sed 's%\(.*\)%    \1%g'|\
"${un_bb}" sed 's%^    \(.*:.*\)%\1%')"|\
"${un_bb}" sed 's|^\]c:#[^\[][^\[]*\[\([^/][^/]*\)\]/c\[$|\1|g';