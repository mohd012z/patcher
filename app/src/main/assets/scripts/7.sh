main_func() {
local F="$1";
local do_log="$2";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local sign_key="${util_dir}/keys/releasekey.x509.pem ${util_dir}/keys/releasekey.pk8";
local un_bb="${util_dir}/busybox";
local un_zia="${util_dir}/zipalign";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to
    \"${j_path}\"!";
fi
local start_at="$(date +%H:%M:%S)";
setspan_green "
  Compression level checker working...
";
test_zip() {
local test_zfile="$@";
local head_er="$("${un_bb}" hexdump -n 4 -C "$test_zfile" |"${un_bb}" head -n1 |"${un_bb}" cut -f3,4,5,6 -d ' ')";
[ "$head_er" = "50 4b 03 04" ] && echo -n "zip";
}
[ "$(test_zip "$F")" != "zip" ] && ab_ort "File
    \"${F}\"
is not an zip file!";
f_name="${F##*/}"; f_name="${f_name%.*}";
echo "  File: ${f_name}
";
local log_f="${F}_complev.txt";
local za_full="$("${un_zia}" -cv 4 "$F" |"${un_bb}" sed -e '/^Verifying /d ' -e '/^Verification /d' -e 's/^  *//g')";
local za_ok="$(echo "$za_full" |"${un_bb}" sed -e '/^$/d
/ (BAD)$/d
/ (BAD - [0-9][0-9]*)$/d
/ (OK - compressed)$/d
s| (OK)$||g')";
local za_okc="$(echo "$za_full" |"${un_bb}" sed -e '/^$/d
/ (BAD)$/d
/ (BAD - [0-9][0-9]*)$/d
/ (OK)$/d
s| (OK - compressed)$||g' )";
local za_bad="$(echo "$za_full" |"${un_bb}" sed -e '/^$/d
/ (OK)$/d
/ (OK - compressed)$/d
s| (BAD)$||g
s| (BAD - [0-9][0-9]*)$||g' )";
[ "$do_log" = "log" ] && local i_un="$("${un_bb}" unzip -v "$F")";
local full_log="";
[ -n "$za_bad" ] && local full_log="-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
          Bad files
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
${za_bad}";
[ -n "$za_ok" ] && local full_log="${full_log}
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
       Not compressed
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
${za_ok}";
local min_log="$full_log";

if [ "$do_log" = "log" ]; then
[ -n "$za_okc" ] && local full_log="${full_log}
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
         Compressed
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
${za_okc}";
local full_log="${full_log}
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
        More details
-#-#-#-#-#-#-#-#-#-#-#-#-#-#-
${i_un}";
echo "${full_log}" >"${log_f}";
fi

setspan_blue "${min_log}"|"${un_bb}" sed 's|^\]c:#[^\[][^\[]*\[\([^-][^#].*\)\]/c\[$|\1|g';
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
[ "$do_log" = "log" ] && echo "    File created: ${log_f}";
exit 0
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
[ -e "$1" ] || ab_ort "Object
    \"${1}\"
  does not exist!";
[ -f "$1" ] || ab_ort "Object
    \"${1}\"
  is not a file!";
main_func "$@" || ab_ort "Unknown error!";
exit