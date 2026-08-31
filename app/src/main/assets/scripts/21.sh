main_func() {
local wh_i="$@";
local vic_1="${wh_i}/strings.xml";
[ -f "$vic_1" ] || ab_ort "File
    strings.xml
  not found in
    ${wh_i}";
[ -d "${wh_i%/*}/values" ] || ab_ort "Directory
    ${wh_i%/*}/values
  not found!";
local vic_2="${wh_i%/*}/values/strings.xml";
[ -f "$vic_2" ] || ab_ort "File
    strings.xml
  not found in
    ${wh_i%/*}/values";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local start_at="$(${un_bb} date +%H:%M:%S)";
setspan_green "
  DefLang working...
";
echo "  File:
    ${vic_1}
";
setspan_blue "  Parsing...";
local ou_tfile="${vic_2}_out";
local hol_dr="";
local hol_dr="$("${un_bb}" cat "$vic_1" |\
"${un_se}" -e '1,/<resources>/d' -e '/<\/resources>/d' -e '/^[^[^<][^<]*<item/d' -e 's|^[ \t][ \t]*\(<string\)|\1|g'|\
"${un_bb}" tr -s '\n' '\x00'|\
"${un_se}" 's|>[^<][^<]*<string|>\n<string|g'|\
"${un_se}" -e 's|\"/>$|\"></string>|g' -e 's|\"[ \t][ \t]*/>$|\"></string>|g'|\
"${un_se}" 's|\x00|~eNdOfLiNe~|g')";   
echo "$hol_dr" >"${ou_tfile}.tmp";
local hol_dr="";
echo "
  File:
    ${vic_2}
";
setspan_blue "  Parsing...";
local hol_de="";
local hol_de="$("${un_bb}" cat "$vic_2" |\
"${un_se}" -e '1,/<resources>/d' -e '/<\/resources>/d' -e '/^[^[^<][^<]*<item/d' -e 's|^[ \t][ \t]*\(<string\)|\1|g'|\
"${un_bb}" tr -s '\n' '\x00'|\
"${un_se}" 's|>[^<][^<]*<string|>\n<string|g'|\
"${un_se}" -e 's|\"/>$|\"></string>|g' -e 's|\"[ \t][ \t]*/>$|\"></string>|g'|\
"${un_se}" 's|\x00|~eNdOfLiNe~|g')";
echo "$hol_de" >"$ou_tfile";
local hol_de="";
setspan_blue "
  Combining files...";	
while read lin_e; do
if [ -n "$lin_e" ]; then
local i_name="$(echo -n "$lin_e"|\
"${un_se}" 's|^<string[^>][^>]*name=\"\([^\"][^\"]*\)\".*|\1|g')";
local i_text="$(echo -n "$lin_e"|\
"${un_se}" 's|^<string[^>][^>]*>\([^<][^<]*\)<.*|\1|g')";
local i_formatted="$(echo -n "$lin_e"|\
"${un_se}" 's|^<string[^>][^>]*\(\sformatted=\"[^\"][^\"]*\"\).*|\1|g')";
[ 1"$i_formatted" = 1"$lin_e" ] && local i_formatted="";
if [ -z "$("${un_bb}" grep -e "^<string[^>][^>]*name=\"$i_name\"" "$ou_tfile" 2>/dev/null)" ]; then
if [ -z "$i_text" ]; then
echo '<string name="'$i_name'"></string>' >>"$ou_tfile";
else
echo "<string name=\"$i_name\"${i_formatted}>${i_text}</string>" >>"$ou_tfile";
fi
else
"${un_se}" -i "/^<string[^>][^>]*name=\"$i_name\"/d" "$ou_tfile";
if [ -z "$i_text" ]; then
echo '<string name="'$i_name'"></string>' >>"$ou_tfile";
else
echo "<string name=\"$i_name\"${i_formatted}>${i_text}</string>" >>"$ou_tfile";
fi
fi
fi
done <"${ou_tfile}.tmp";
"${un_bb}" rm -f "${ou_tfile}.tmp";
"${un_se}" -i '1s|.*|<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n\0|' "$ou_tfile";
"${un_se}" -i 's|^<string|    <string|g' "$ou_tfile";
"${un_se}" -i 's|~eNdOfLiNe~|\n|g' "$ou_tfile";
echo "</resources>" >>"$ou_tfile";

if [ -f "${wh_i}/plurals.xml" ]; then
local vic_1="${wh_i}/plurals.xml";
local vic_2="${wh_i%/*}/values/plurals.xml";
local ou_tfile="${vic_2}_out";
echo "
  File:
    ${vic_1}
";
setspan_blue "  Parsing...";
local hol_dr="$("${un_bb}" cat "$vic_1" |"${un_se}" -z 's|\n\n*||g
s|\s\s*\(<plurals name=\)|\n\1|g
s|\(</resources>\)|\n\1|g' |"${un_se}" '1d
/^<\/resources>/d' |"${un_se}" 's|^<plurals name=\"||g
s|\">|EN2RU_STUB_DIV|1')";
echo "$hol_dr" >"${ou_tfile}.tmp";
local hol_dr="";
echo "
  File:
    ${vic_2}
";
setspan_blue "  Parsing...";
local hol_de="$("${un_bb}" cat "$vic_2" |"${un_se}" -z 's|\n\n*||g
s|\s\s*\(<plurals name=\)|\n\1|g
s|\(</resources>\)|\n\1|g' |"${un_se}" '/^<\/resources>/d')";
echo "$hol_de" >"$ou_tfile";
local hol_de="";
setspan_blue "
  Combining files...";
while read lin_e; do
if [ -n "$lin_e" ]; then
local i_name="${lin_e%%EN2RU_STUB_DIV*}";
local i_text="${lin_e##*EN2RU_STUB_DIV}";
if [ -z "$("${un_bb}" grep -e "^<plurals name=\"$i_name\"" "$ou_tfile" 2>/dev/null)" ]; then
echo "<plurals name=\"$i_name\">${i_text}" >>"$ou_tfile";
else
"${un_bb}" sed -i "/^<plurals name=\"$i_name\"/d" "$ou_tfile";
echo "<plurals name=\"$i_name\">${i_text}" >>"$ou_tfile";
fi
fi
done <"${ou_tfile}.tmp";
"${un_bb}" rm -f "${ou_tfile}.tmp";
echo "</resources>" >>"$ou_tfile";
"${un_se}" -i '1s|\(<resources>\)|\n\1|
s|^\(<plurals name=\"\)|    \1|g
s|>\(\s\s*<item [a-z][a-z]*=\"\)|>\n\1|g
s|>\(\s\s*</plurals>\)|>\n\1|g' "$ou_tfile";
fi

if [ -f "${wh_i}/arrays.xml" ]; then
local vic_1="${wh_i}/arrays.xml";
local vic_2="${wh_i%/*}/values/arrays.xml";
local ou_tfile="${vic_2}_out";
echo "
  File:
    ${vic_1}
";
setspan_blue "  Parsing...";
local hol_dr="$("${un_bb}" cat "$vic_1" |"${un_se}" -z 's|\n\n*||g
s|\s\s*\(<[a-z\-][a-z\-]*rray name=\)|\n\1|g
s|\(</resources>\)|\n\1|g' |"${un_se}" '1d
/^<\/resources>/d' |"${un_se}" 's|^<\([a-z\-][a-z\-]*rray\) name=\"|\1EN2RU_STUB_DIV|g
s|\">|EN2RU_STUB_DIV|1')";
echo "$hol_dr" >"${ou_tfile}.tmp";
local hol_dr="";
echo "
  File:
    ${vic_2}
";
setspan_blue "  Parsing...";
local hol_de="$("${un_bb}" cat "$vic_2" |"${un_se}" -z 's|\n\n*||g
s|\s\s*\(<[a-z\-][a-z\-]*rray name=\)|\n\1|g
s|\(</resources>\)|\n\1|g' |"${un_se}" '/^<\/resources>/d')";
echo "$hol_de" >"$ou_tfile";
local hol_de="";
setspan_blue "
  Combining files...";
while read lin_e; do
if [ -n "$lin_e" ]; then
local i_type="${lin_e%%EN2RU_STUB_DIV*}";
local i_name="${lin_e%EN2RU_STUB_DIV*}";
local i_name="${i_name##*EN2RU_STUB_DIV}";
local i_text="${lin_e##*EN2RU_STUB_DIV}";
if [ -z "$("${un_bb}" grep -e "^<$i_type name=\"$i_name\"" "$ou_tfile" 2>/dev/null)" ]; then
echo "<$i_type name=\"$i_name\">${i_text}" >>"$ou_tfile";
else
"${un_bb}" sed -i "/^<$i_type name=\"$i_name\"/d" "$ou_tfile";
echo "<$i_type name=\"$i_name\">${i_text}" >>"$ou_tfile";
fi
fi
done <"${ou_tfile}.tmp";
"${un_bb}" rm -f "${ou_tfile}.tmp";
echo "</resources>" >>"$ou_tfile";
"${un_se}" -i '1s|\(<resources>\)|\n\1|
s|^\(<[a-z\-][a-z\-]*rray name=\"\)|    \1|g
s|>\(\s\s*<item>\)|>\n\1|g
s|>\(\s\s*</[a-z\-][a-z\-]*rray>\)|>\n\1|g' "$ou_tfile";
fi
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(${un_bb} date +%H:%M:%S)
         Created: $(echo "${wh_i%/*}"/values/*.xml_out)";
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
ex_it() {
cd - >/dev/null 2>&1;
exit
}
[ -z "$@" ] && ab_ort "No arguments passed!";
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -d "$@" ] || ab_ort "Object
    \"${@}\"
  is not a directory!";
main_func "$@"
exit