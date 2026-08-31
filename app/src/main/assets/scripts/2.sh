main_func() {
local F="$(pwd)";
setspan_green "
  Antilog working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local un_du="${util_dir}/mdu";
local start_at="$(date +%H:%M:%S)";
local s_before="$("${un_du}" "$F")";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}
";
setspan_blue "  Searching in smali files...";
local sm_arr="$("${un_bb}" find ./smali*/ -type f -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_bb}" grep -l "^  *invoke-[a-z][a-z]* .*, Landroid/util/Log;->.*$")";
local sm_num="$(echo "$sm_arr" |"${un_bb}" wc -l)";
if [ -z "$sm_arr" ]; then
echo "  Found 0 files.";
setspan_blue "  Nothing to do!";
else
echo "  Found ${sm_num} files.";
setspan_blue "  Patching smali...";
{
echo -n $sm_arr |"${un_bb}" xargs -r -n 999 -P 4 "${un_se}" -zi 's|\n  *invoke-[a-z][a-z]* [pv0-9\{\} ,][pv0-9\{\} ,]*, Landroid/util/Log;->\S\S*\n\n*  *move-resul\S\S* \([p,v][0-9][0-9]*\)|\n    const/4 \1, 0x0\n|g
s|\n  *invoke-[a-z][a-z]* [pv0-9\{\} ,][pv0-9\{\} ,]*, Landroid/util/Log;->\S\S*\n||g'
} 2>/dev/null
fi
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
    Removed data: $(($((${s_before} - $("${un_du}" "$F")))/1024)) Kb";
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