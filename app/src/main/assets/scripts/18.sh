main_func() {
setspan_green "
  Disconnect working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}
";
setspan_blue "  Patching smali...";
{
"${un_bb}" find ./smali*/ -type f -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_se}" -zi 's|\n  *invoke-[a-z][a-z]* [pv0-9 ,\{\}][pv0-9 ,\{\}]*, Landroid/net/ConnectivityManager;->getActiveNetworkInfo\S\S*\s\s*move-resul[a-z\-][a-z\-]* \([pv][0-9][0-9]*\)\n|\n    const/4 \1, 0x0\n|g
s|\n  *invoke-[a-z][a-z]* [pv0-9 ,\{\}][pv0-9 ,\{\}]*, Landroid/net/NetworkInfo;->\S\S*\s\s*move-resul[a-z\-][a-z\-]* \([pv][0-9][0-9]*\)\n|\n    const/4 \1, 0x0\n|g'
} 2>/dev/null
setspan_blue "  Patching AndroidManifest.xml...";
"${un_se}" -zi 's|\(\s\s*\)<uses-permission\s\s*android:name=\"android\.permission\.INTERNET\"[ /][ /]*>\s\s*|\1|g' ./AndroidManifest.xml
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
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