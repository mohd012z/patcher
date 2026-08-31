main_func() {
local F="$@";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local sign_key="${util_dir}/keys/releasekey.x509.pem ${util_dir}/keys/releasekey.pk8";
local un_bb="${util_dir}/busybox";
local un_sapk="${util_dir}/apksigner";
local un_zia="${util_dir}/zipalign";
local un_arg="${util_dir}/AndResGuard";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to
    \"$j_path\"!
";
fi
local start_at="$(date +%H:%M:%S)";
local confi_g="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48cmVzcHJvZ3VhcmQ+PGlzc3VlIGlkPSJwcm9wZXJ0eSI+PHNldmVudHppcCB2YWx1ZT0iZmFsc2UiLz48bWV0YW5hbWUgdmFsdWU9Ik1FVEEtSU5GIi8+PGtlZXByb290IHZhbHVlPSJmYWxzZSIvPjwvaXNzdWU+PGlzc3VlIGlkPSJ3aGl0ZWxpc3QiIGlzYWN0aXZlPSJmYWxzZSIvPjxpc3N1ZSBpZD0ia2VlcG1hcHBpbmciIGlzYWN0aXZlPSJmYWxzZSIvPjxpc3N1ZSBpZD0iY29tcHJlc3MiIGlzYWN0aXZlPSJmYWxzZSIvPjxpc3N1ZSBpZD0ic2lnbiIgaXNhY3RpdmU9ImZhbHNlIi8+PC9yZXNwcm9ndWFyZD4=";
[ -d "${util_dir}/andresguardtmp" ] && rm -rf "${util_dir}/andresguardtmp";
"${un_bb}" mkdir -p "${util_dir}/andresguardtmp" || ab_ort "Can't create temp directory!";
echo -n "$confi_g"|"${un_bb}" base64 -d >"${util_dir}/andresguardtmp/config.xml";
setspan_green "
  AndResGuard working...
";
test_zip() {
local test_zfile="$@";
local head_er="$("${un_bb}" hexdump -n 4 -C "$test_zfile" |"${un_bb}" head -n1 |"${un_bb}" cut -f3,4,5,6 -d ' ')";
[ "$head_er" = "50 4b 03 04" ] && echo -n "zip";
}
[ "$(test_zip "$F")" != "zip" ] && ab_ort "File
    \"${F}\"
  is not zip file!";
local f_name="${F##*/}";
local f_name="${f_name%.*}";
echo "  File: ${f_name}
";
setspan_blue "  Obfuscate resources...
";
"${un_arg}" "$F" -config "${util_dir}/andresguardtmp/config.xml" -out "${util_dir}/andresguardtmp"
local j_newfile="$(echo -n "${util_dir}/andresguardtmp/"*_unsigned.*)";
[ -f "$j_newfile" ] || { "${un_bb}" rm -rf "${util_dir}/andresguardtmp"; ab_ort "Can't find any resulting 'apk' file!"; }
setspan_blue "
  Zipalign file...";
[ -f "${j_path}/${f_name}_zipalign" ] && "${un_bb}" rm -f "${j_path}/${f_name}_zipalign";
"${un_zia}" -p -f 4 "$j_newfile" "${j_path}/${f_name}_zipalign";
"${un_bb}" rm -rf "${util_dir}/andresguardtmp";
setspan_blue "  Sign file...";
[ -f "${j_path}/${f_name}_obf.${F##*.}" ] && "${un_bb}" rm -f "${j_path}/${f_name}_obf.${F##*.}";
"${un_sapk}" $sign_key "${j_path}/${f_name}_zipalign" "${j_path}/${f_name}_obf.${F##*.}" >/dev/null;
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
[ -z "$@" ] && ab_ort "No arguments passed!";
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -f "$@" ] || ab_ort "Object
    \"${@}\"
  is not a file!";
main_func "$@" || ab_ort "Unknown error!";
exit