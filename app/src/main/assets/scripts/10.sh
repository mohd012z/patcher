main_func() {
local F="$@";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to
    \"${j_path}\"!";
fi
local start_at="$(date +%H:%M:%S)";
setspan_green "
  Add binary working...
";

### testing zip ###

test_zip() {
local test_zfile="$F";
local head_er="$("${un_bb}" hexdump -n 4 -C "$test_zfile" |"${un_bb}" head -n1 |"${un_bb}" cut -f3,4,5,6 -d ' ')";
[ "$head_er" = "50 4b 03 04" ] && echo -n "zip";
}
[ "$(test_zip "$F")" != "zip" ] && ab_ort "File
    \"${F}\"
is not an zip file!";
local f_name="${F##*/}";
echo "  File: ${f_name}";
local file_list="$("${un_bb}" unzip -qql "$F" "*" 2>/dev/null |"${un_bb}" awk '{print $4}' 2>/dev/null)";
[ -z "$(echo "$file_list" |"${un_bb}" grep "THISMPATCHERBINARYZIP" 2>/dev/null)" ] && ab_ort "File
    \"${f_name}\"
  is not MSAPatcher binary zip!";
local file_list="$(echo "$file_list" |"${un_bb}" sed -e '/THISMPATCHERBINARYZIP/d' 2>/dev/null)";
[ -z "$file_list" ] && ab_ort "File
    \"${f_name}\"
  is empty MSAPatcher binary zip!";

### testing device ###

local z_path="";
if [ -z "$(echo "$file_list" |"${un_bb}" grep "any-arch" 2>/dev/null)" ]; then
local ar_ch="$("${un_bb}" arch 2>/dev/null)";
[ -z "$ar_ch" ] && ab_ort "Failed to execute
\"${un_bb} arch\"";
echo "  Kernel: ${ar_ch}";
local ar_ch="$("${un_bb}"|"${un_bb}" head -n1|"${un_bb}" sed 's|.*-\([armx326486][armx326486]*\).*|\1|')";
[ "$ar_ch" = "arm64" ] && local z_path="arm64-v8a";
[ "$ar_ch" = "arm32" ] && local z_path="armeabi-v7a";
[ "$ar_ch" = "x86" ] && local z_path="x86";
[ "$ar_ch" = "x64" ] && local z_path="x86_64";
[ -z "$z_path" ] && ab_ort "It is impossible to determine the bitness of the OS!";
echo "  Arch: ${z_path}
";
else
local z_path="any-arch";
fi

### testing addons ###

if [ "$z_path" = "armeabi-v7a" ]; then
  [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ] && ab_ort "MSAPatcher binary zip does not contain utilities suitable for your device!";
else
  if [ "$z_path" = "arm64-v8a" ]; then
    if [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ]; then
      echo "  MSAPatcher binary zip does not contain utilities suitable for 'arm64-v8a'!
  Let's try 'armeabi-v7a'...
";
      local z_path="armeabi-v7a";
      [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ] && ab_ort "MSAPatcher binary zip does not contain utilities suitable for your device!";
    fi
  else
    if [ "$z_path" = "x86" ]; then
      [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ] && ab_ort "MSAPatcher binary zip does not contain utilities suitable for your device!";
    else
      if [ "$z_path" = "x86_64" ]; then
        if [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ]; then
          echo "  MSAPatcher binary zip does not contain utilities suitable for 'x86_64'!
  Let's try 'x86'...
";
          local z_path="x86";
          [ -z "$(echo "$file_list" |"${un_bb}" grep "${z_path}/" 2>/dev/null)" ] && ab_ort "MSAPatcher binary zip does not contain utilities suitable for your device!";
        fi
      fi
    fi
  fi
fi

local file_list="$(echo "$file_list" |"${un_bb}" grep "${z_path}" 2>/dev/null)";
setspan_blue "  Installing:";
for b_file in $file_list; do
"${un_bb}" unzip -oq "$F" "$b_file" -d "${util_dir}";
done
cd "${util_dir}/${z_path}" || ab_ort "Can not 'cd' to
    \"${util_dir}/${z_path}\"";
for b_file in $("${un_bb}" ls -A); do
echo -n "    ${b_file} - ";
"${un_bb}" mv -f "${b_file}" "${util_dir}";
[ -e "${util_dir}/${b_file}" ] && setspan_green "Ok!" || setspan_red "False!";
"${un_bb}" chmod -R 500 "${util_dir}/${b_file}" || ab_ort "Can not give permissions to file!";
done
[ -n "${util_dir}/${z_path}" ] && "${un_bb}" rm -rf "${util_dir}/${z_path}";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
exit
}
mpatcher_dir() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
echo "
  Installed tools:"
"${un_bb}" ls -lhR --full-time "${util_dir}" |"${un_bb}" sed -e '/^total [0-9][0-9]*/d
/^$/d' |"${un_bb}" awk '{print $9}{print $1,$5,$6,$7}' |"${un_bb}" sed -e 's%\(.*\)%    \1%g' -e 's%^    \(.*\)/\(.*\)%\1/\2%g
s%    \(.* .* .* .*\)%\1%g';
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
[ -z "$@" ] && { mpatcher_dir; ab_ort "

  No arguments passed!"; };
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -f "$@" ] || ab_ort "Object
    \"${@}\"
  is not a file!";
main_func "$@" || ab_ort "Unknown error!";
exit