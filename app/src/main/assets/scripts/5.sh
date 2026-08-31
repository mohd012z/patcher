main_func() {
local F="$@";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local sign_key="${util_dir}/keys/releasekey.x509.pem ${util_dir}/keys/releasekey.pk8";
local un_bb="${util_dir}/busybox";
local un_zi="${util_dir}/ZipR";
local un_sapk="${util_dir}/apksigner";
local un_zia="${util_dir}/zipalign";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to \"${j_path}\"!";
fi
local start_at="$(date +%H:%M:%S)";
local arg_arr="0";
local last_app="";
setspan_green "
  UnArch working...
";
test_zip() {
local test_zfile="$@";
local head_er="$("${un_bb}" hexdump -n 4 -C "$test_zfile" |"${un_bb}" head -n1 |"${un_bb}" cut -f3,4,5,6 -d ' ')";
[ "$head_er" = "50 4b 03 04" ] && echo -n "zip";
}
[ "$(test_zip "$F")" != "zip" ] && ab_ort "File
    \"${F}\"
is not an apk file!";
local f_name="${F##*/}";
local f_name="${f_name%.*}";
echo "   App: ${f_name}
  Size: $(($("${un_bb}" stat -c %s "$F")/1024)) Kb";
local lib_list1="$("${un_bb}" unzip -qql "$F" "lib/*" 2>/dev/null |"${un_bb}" sed -e "s|.* ||g")";
local lib_list1="$(echo "$lib_list1" |"${un_bb}" sed -e "/^$/d")";
local lib_list="";
for FF in $lib_list1; do
local n_part="${FF#lib/}";
local n_part="${n_part%/*}";
[ -z "$(echo "$lib_list" |"${un_bb}" grep -e "^${n_part}$" 2>/dev/null)" ] && local lib_list="$lib_list
$n_part";
done
local lib_list="$(echo "$lib_list" |"${un_bb}" sed -e "/^$/d" -e "s/.*/    \0/g" 2>/dev/null)";
setspan_blue "
  Include lib's:";
echo "${lib_list}
";
${un_zi} "${F}" || exit 0;
for FF in arm64-v8a arm64-v8a armeabi-v7a armeabi mips64 mips x86_64 x86; do
local Fbase="${F%.*}_${FF}_unarch.apk";
if [ -f "$Fbase" ]; then
local arg_arr=$(($arg_arr + 1));
local the_new_guy="${F%.*}_${FF}.apk";
setspan_blue "  Creating:";
echo "\"$the_new_guy\"";
setspan_blue "  Zipalign file...";
[ -f "${the_new_guy}" ] && "${un_bb}" rm -f "${the_new_guy}";
"${un_zia}" -p -f 4 "$Fbase" "${the_new_guy}.s";
"${un_bb}" rm -f "$Fbase";
setspan_blue "  Sign file...";
"${un_sapk}" $sign_key "${the_new_guy}.s" "${the_new_guy}" >/dev/null;
"${un_bb}" rm -f "${the_new_guy}.s";
echo "  New size: $(($("${un_bb}" stat -c %s "$the_new_guy")/1024)) Kb
";
fi
done
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
   Parts Created: ${arg_arr}";
exit
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
[ -z "$(echo -n "$@")" ] && ab_ort "No arguments passed!";
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -f "$@" ] || ab_ort "Object
    \"${@}\"
  is not a file!";
main_func "$@" || ab_ort "Unknown error!";
exit