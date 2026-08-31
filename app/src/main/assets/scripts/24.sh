main_func() {
local base_s="./values/strings.xml";
local patt_file="../stringstub.txt";
setspan_green "
  StringStub working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local s_count="0";
local f_name="${F%/res}";
local f_name="${f_name##*/}";
echo "  App: ${f_name%_src}
";
"${un_bb}" dos2unix "$patt_file";
echo "">>"$patt_file";
"${un_se}" -i '/^$/d' "$patt_file";
while read pat_t; do
local pat_tt="";
if [ -n "$pat_t" ]; then
local pat_tt="${pat_t//\*/[^\\\"][^\\\"]\*}";
local pat_tt="$("${un_bb}" grep -e "<string name=\"$pat_tt\"" "$base_s"|"${un_se}" 's|.*<string name=\"\([^\"][^\"]*\)\".*|\1|g')";
if [ -n "$pat_tt" ]; then
for lin_e in $pat_tt; do
echo -n "  Removing ";
setspan_blue "\"${lin_e}\"";
local s_count="$((${s_count} + 1))";
"${un_se}" -i "/<string name=\"$lin_e\">/,/<\/string>/d" ./values*/strings.xml;
"${un_se}" -i "s|.*</resources>.*|    <string name=\"$lin_e\" \/>\n<\/resources>|g" "$base_s";
done
fi
fi
done <"$patt_file";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
 Deleted strings: ${s_count}";
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
[ -d "${@}/res" ] || ab_ort "Directory
    \"${@}/res\"
  is not exist!
  You must decompile package resources!";
[ -f "${@}/stringstub.txt" ] || ab_ort "File
    \"${@}/stringstub.txt\"
  does not exist!";
cd "${@}/res" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${@}\"!";
[ -f "${@}/res/values/strings.xml" ] || ab_ort "File
    \"${@}/res/values/strings.xml\"
  does not exist!";
main_func
cd - >/dev/null 2>&1;
exit