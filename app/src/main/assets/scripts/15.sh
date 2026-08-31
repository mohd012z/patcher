main_func() {
setspan_green "
  UniString working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_un="${util_dir}/uni";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local b_c_f=0;
local f_name="${F##*/}";
echo "  App: ${f_name%_src}";
setspan_blue "  Searching for unicode...";
local file_arr="";
local file_arr="$("${un_bb}" find ./smali*/ -type f -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_bb}" grep -l -e "  *const-string [pv][0-9][0-9]*, \".*\\\u[a-f0-9][a-f0-9][a-f0-9][a-f0-9]")";
[ -z "$file_arr" ] && ab_ort "This app does not contain unicode!";
setspan_blue "  Patching smali...";

"${un_un}" $file_arr

local b_c_f="$(echo "$file_arr"|"${un_bb}" wc -l)";

echo "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
   Files patched: ${b_c_f}";
}
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
ab_ort() {
local tx_t="$@";
setspan_red "
  ${tx_t}
";
exit
}
[ -z "$@" ] && ab_ort "No arguments passed!";
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -d "$@" ] || ab_ort "Object
    \"${@}\"
  is not a directory!";
[ -d "${@}/smali" ] || ab_ort "Directory
    \"${@}\"
  does not contain smali!";
cd "${@}" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${@}\"!";
main_func
cd - >/dev/null 2>&1;
exit
