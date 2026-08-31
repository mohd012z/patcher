main_func() {
setspan_green "
  DummyAway working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local F="$(pwd)";
local F="${F%/res}";
local start_at="$(date +%H:%M:%S)";
local be_fore="$("${un_bb}" find . -type f -iname "*.xml"|"${un_bb}" wc -l)";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}";
local sm_arr="$("${un_bb}" find . -type f -iname "*.xml" -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_bb}" grep -l "name=\"APKTOOL_DUMMY")";
local sm_arr="$sm_arr
$("${un_bb}" find . -type f -iname "*.xml" -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_bb}" grep -l "name=\"dummy_ae")";
local sm_arr="$(echo "$sm_arr"|"${un_bb}" sed '/^$/d')";
if [ -z "$sm_arr" ]; then
local sm_num=0;
echo "  Found 0 files.";
setspan_blue "  Nothing to do!";
else
local sm_num="$(($("${un_bb}" find . -type f -iname "*.xml" -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_bb}" grep -le "APKTOOL_DUMMY"|"${un_bb}" wc -l) + $("${un_bb}" find . -type f -iname "*.xml" -print0|"${un_bb}" xargs -0 -r -n 150 -P 4 "${un_bb}" grep -le "dummy_ae"|"${un_bb}" wc -l)))";
echo "  Found ${sm_num} files.";
setspan_blue "  Patching...";
echo -n $sm_arr |"${un_bb}" xargs -r -n 999 -P 4 "${un_se}" -i '/name=\"APKTOOL_DUMMY/d
/name=\"dummy_ae/d'
"${un_bb}" find . -type f -iname "*.xml" -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_se}" -i 's|=\"[^\"][^\"]*/APKTOOL_DUMMY[^\"][^\"]*\"|=\"@null"|g
s|=\"[^\"][^\"]*/dummy_ae[^\"][^\"]*\"|=\"@null"|g
s|>[^<][^<]*/APKTOOL_DUMMY[^<][^<]*<|>@null<|g
s|>[^<][^<]*/dummy_ae[^<][^<]*<|>@null<|g'
for vi_c in $sm_arr; do
[ -z "$("${un_bb}" grep "name=" "$vi_c" 2>/dev/null)" ] && "${un_bb}" rm -f "$vi_c";
done
fi
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
 Files processed: ${sm_num}
   Files deleted: $((${be_fore} - $("${un_bb}" find . -type f -iname "*.xml"|"${un_bb}" wc -l)))";
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
cd "${@}/res" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${@}/res\"!";
main_func
cd - >/dev/null 2>&1;
exit