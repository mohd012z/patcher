main_func() {
local F="$@";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local sign_key="${util_dir}/keys/releasekey.x509.pem ${util_dir}/keys/releasekey.pk8";
local un_bb="${util_dir}/busybox";
local un_sapk="${util_dir}/apksigner";
local un_zia="${util_dir}/zipalign";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to
    \"$j_path\"!
";
fi
local start_at="$(date +%H:%M:%S)";
setspan_green "
  Apksigner working...
";
test_zip() {
local test_zfile="$@";
local head_er="$("${un_bb}" hexdump -n 4 -C "$test_zfile" |"${un_bb}" head -n1 |"${un_bb}" cut -f3,4,5,6 -d ' ')";
[ "$head_er" = "50 4b 03 04" ] && echo -n "zip";
}
[ "$(test_zip "$F")" != "zip" ] && ab_ort "File
    \"${F}\"
  is not zip file!";
f_name="${F##*/}"; f_name="${f_name%.*}";
echo "  File: ${f_name}
";
setspan_blue "  Zipalign file...";
[ -f "${j_path}/${f_name}_zipalign" ] && "${un_bb}" rm -f "${j_path}/${f_name}_zipalign";
"${un_zia}" -p -f 4 "$F" "${j_path}/${f_name}_zipalign";
setspan_blue "  Sign file...";
[ -f "${j_path}/${f_name}_zipalign_sign.${F##*.}" ] && "${un_bb}" rm -f "${j_path}/${f_name}_zipalign_sign.${F##*.}";
"${un_sapk}" $sign_key "${j_path}/${f_name}_zipalign" "${j_path}/${f_name}_zipalign_sign.${F##*.}" >/dev/null;
[ -f "${j_path}/${f_name}_zipalign" ] && "${un_bb}" rm -f "${j_path}/${f_name}_zipalign";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
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