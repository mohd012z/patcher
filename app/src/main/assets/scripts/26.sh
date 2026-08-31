main_func() {
setspan_green "
  TrashClasses working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local un_du="${util_dir}/mdu";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local s_before="$("${un_du}" "$F")";
local f_name="${F##*/}";
local b_c_f=0;
local del_list="";
local del_list_b="";
local job_list="";
local job_list_b="";
local job_list_u="";
echo "  App: ${f_name%_src}
";
setspan_blue "  Creating smali database...";
echo "$("${un_bb}" find ./smali*/ -type f -iname "*.smali" -print0|"${un_bb}" xargs -0 -r -n 999 -P 8 "${un_bb}" grep -lE '/(BuildConfig|R|R\$[a-z][a-z]*);')" >"${0%/*}/temp_xargs";
"${un_se}" -zi 's|\n|\x00|g' "${0%/*}/temp_xargs";
"${un_bb}" echo -n "bWFpbl9mdW5jKCkgewpsb2NhbCBhcmdzX2Fycj0iJEAiOwpsb2NhbCBwYXRfaD0iJHthcmdzX2FyciUlPSp9IjsKbG9jYWwgcl9sb2M9IiR7YXJnc19hcnIjIyo9fSI7CmxvY2FsIHV0aWxfZGlyPSIkezAlLyp9L2JpbiI7CmxvY2FsIHVuX2JiPSIke3V0aWxfZGlyfS9idXN5Ym94IjsKbG9jYWwgdW5fc2U9IiR7dXRpbF9kaXJ9L3NlZCI7CmNkICIkcGF0X2giIHx8IGV4aXQgMDsKbG9jYWwgc2VhcmNoX2Fycj0iJCgiJHt1bl9iYn0iIGVjaG8gLW4gIiR7cl9sb2N9L1IiKi5zbWFsaXwiJHt1bl9zZX0iICdzfFwuc21hbGkgfFwuc21hbGlcbnxnJ3wiJHt1bl9iYn0iIGdyZXAgLUUgJy8oUnxSXCRbYS16XVthLXpdKilcLnNtYWxpJCcpIjsKbG9jYWwgcl9sb2NfbnVtPSIkKCIke3VuX2JifSIgZWNobyAiJHNlYXJjaF9hcnIifCIke3VuX2JifSIgd2MgLWwpIjsKZm9yIG9uZV9vZl9yIGluICRzZWFyY2hfYXJyOyBkbwpsb2NhbCB0X3BhdHQ9IiQoIiR7dW5fYmJ9IiBoZWFkIC1uMSAiJG9uZV9vZl9yInwiJHt1bl9zZX0iICdzfC4qIFwoW14gXVteIF0qXCkkfFwxfGcnKSI7CmxvY2FsIHJfb25lX29mX251bT0iJCgiJHt1bl9iYn0iIHhhcmdzIC0wIC1yIC1hICIkezAlLyp9L3RlbXBfeGFyZ3MiIC1uIDIwMCAtUCA0ICIke3VuX2JifSIgZ3JlcCAtbCAiJHRfcGF0dCJ8IiR7dW5fYmJ9IiB3YyAtbCkiOwppZiBbICIke29uZV9vZl9yIyMqL30iID0gIlIuc21hbGkiIF07IHRoZW4KaWYgWyAiJHJfbG9jX251bSIgPSAiJHJfb25lX29mX251bSIgXTsgdGhlbgppZiBbIC16ICIkZGVsX2xpc3QiIF07IHRoZW4KbG9jYWwgZGVsX2xpc3Q9IiRvbmVfb2ZfciI7CmVsc2UKbG9jYWwgZGVsX2xpc3Q9IiRkZWxfbGlzdAokb25lX29mX3IiOwpmaQpmaQplbHNlCmlmIFsgIjIiIC1nZSAiJHJfb25lX29mX251bSIgXTsgdGhlbgppZiBbIC16ICIkZGVsX2xpc3QiIF07IHRoZW4KbG9jYWwgZGVsX2xpc3Q9IiRvbmVfb2ZfciI7CmVsc2UKbG9jYWwgZGVsX2xpc3Q9IiRkZWxfbGlzdAokb25lX29mX3IiOwpmaQpmaQpmaQpkb25lCiIke3VuX2JifSIgZWNobyAiJGRlbF9saXN0Igp9Cm1haW5fZnVuYyAiJEAiOwpleGl0IDAK"|"${un_bb}" base64 -d >"${0}_part1";
"${un_bb}" chmod 755 "${0}_part1";
"${un_bb}" echo -n "bWFpbl9mdW5jKCkgewpsb2NhbCBhcmdzX2Fycj0iJEAiOwpsb2NhbCBwYXRfaD0iJHthcmdzX2FyciUlPSp9IjsKbG9jYWwgb25lX29mPSIke2FyZ3NfYXJyIyMqPX0iOwpsb2NhbCB1dGlsX2Rpcj0iJHswJS8qfS9iaW4iOwpsb2NhbCB1bl9iYj0iJHt1dGlsX2Rpcn0vYnVzeWJveCI7CmxvY2FsIHVuX3NlPSIke3V0aWxfZGlyfS9zZWQiOwpsb2NhbCBkZV9saXN0PSIiOwpjZCAiJHBhdF9oIiB8fCBleGl0IDA7CmxvY2FsIHRfcGF0dD0iJCgiJHt1bl9iYn0iIGhlYWQgLW4xICIkb25lX29mInwiJHt1bl9zZX0iICdzfC4qIFwoW14gXVteIF0qXCkkfFwxfGcnKSI7CmxvY2FsIHJfb25lX29mX251bT0iJCgiJHt1bl9iYn0iIHhhcmdzIC0wIC1yIC1hICIkezAlLyp9L3RlbXBfeGFyZ3MiIC1uIDIwMCAtUCA0ICIke3VuX2JifSIgZ3JlcCAtbCAiJHRfcGF0dCIpIjsKbG9jYWwgcl9vbmVfb2ZfbnVtPSIkKCIke3VuX2JifSIgZWNobyAiJHJfb25lX29mX251bSJ8IiR7dW5fYmJ9IiB3YyAtbCkiOwppZiBbICIxIiA9ICIkcl9vbmVfb2ZfbnVtIiBdOyB0aGVuCmlmIFsgLXogIiRkZV9saXN0IiBdOyB0aGVuCmxvY2FsIGRlX2xpc3Q9IiRvbmVfb2YiOwplbHNlCmxvY2FsIGRlX2xpc3Q9IiRkZV9saXN0CiRvbmVfb2YiOwpmaQpmaQoiJHt1bl9iYn0iIGVjaG8gIiRkZV9saXN0Igp9Cm1haW5fZnVuYyAiJEAiOwpleGl0IDAK"|"${un_bb}" base64 -d >"${0}_part2";
"${un_bb}" chmod 755 "${0}_part2";
"${un_bb}" echo -n "bWFpbl9mdW5jKCkgewpsb2NhbCBhcmdzX2Fycj0iJEAiOwpsb2NhbCBwYXRfaD0iJHthcmdzX2FyciUlPSp9IjsKbG9jYWwgb25lX29mX3U9IiR7YXJnc19hcnIjIyo9fSI7CmxvY2FsIHV0aWxfZGlyPSIkezAlLyp9L2JpbiI7CmxvY2FsIHVuX2JiPSIke3V0aWxfZGlyfS9idXN5Ym94IjsKbG9jYWwgdW5fc2U9IiR7dXRpbF9kaXJ9L3NlZCI7CmNkICIkcGF0X2giIHx8IGV4aXQgMDsKZm9yIGVhY2hfc3RyaW5nIGluICQoIiR7dW5fYmJ9IiBncmVwICdeICAqW2Etel1nZXQgW3Asdl1bMC05XVswLTldKiwgTC4qL1JcJFthLXpdW2Etel0qOy0nICIkb25lX29mX3UifCIke3VuX3NlfSIgJ3N8Xi4qIHx8ZycpOyBkbwpsb2NhbCB0ZXN0X3BvaW50PWRvCmxvY2FsIHdfY2xhc3M9IiQoIiR7dW5fYmJ9IiBlY2hvIC1uICIkZWFjaF9zdHJpbmcifCIke3VuX3NlfSIgJ3N8XkxcKFteO11bXjtdKlwpOy4qfFwxfGcnfCIke3VuX2JifSIgaGVhZCAtbiAxKSI7CmxvY2FsIHdfZmlsZT0iJCgiJHt1bl9iYn0iIGVjaG8gLW4gLi9zbWFsaSovJHt3X2NsYXNzfS5zbWFsaSkiOwpsb2NhbCB3X2FyZz0iJCgiJHt1bl9iYn0iIGVjaG8gLW4gIiRlYWNoX3N0cmluZyJ8IiR7dW5fc2V9IiAnc3xeW14+XVtePl0qPlwoLio6SVwpJHxcMXxnJ3wiJHt1bl9iYn0iIGhlYWQgLW4gMSkiOwpsb2NhbCB3X2ludD0iJCgiJHt1bl9iYn0iIGdyZXAgLXMgIl5cLmZpZWxkIC4qICR3X2FyZyA9ICIgIiR3X2ZpbGUifCIke3VuX3NlfSIgJ3N8Xi4qPSAgKlwoLipcKSR8XDF8Zyd8IiR7dW5fYmJ9IiBoZWFkIC1uIDEpIjsKWyAteiAiJHdfY2xhc3MiIF0gJiYgbG9jYWwgdGVzdF9wb2ludD1za2lwOwpbIC16ICIkd19hcmciIF0gJiYgbG9jYWwgdGVzdF9wb2ludD1za2lwOwpbIC16ICIkd19pbnQiIF0gJiYgbG9jYWwgdGVzdF9wb2ludD1za2lwOwpbICIkdGVzdF9wb2ludCIgPSAiZG8iIF0gJiYgewpsb2NhbCB0ZXN0X3BvaW50PSJjb25zdCI7CiIke3VuX3NlfSIgLWkgInN8XiAgKlthLXpdZ2V0IFwoW3Asdl1bMC05XVswLTldKixcKSBMJHdfY2xhc3M7LT4kd19hcmckfCMjI3RyYXNoXG4gICAgJHRlc3RfcG9pbnQgXDEgJHdfaW50fGciICIkb25lX29mX3UiOwp9CmRvbmUKfQptYWluX2Z1bmMgIiRAIgpleGl0IDAK"|"${un_bb}" base64 -d >"${0}_part3";
"${un_bb}" chmod 755 "${0}_part3";
setspan_blue "  Removing some invokes...";
local used_array="$("${un_bb}" xargs -0 -r -a "${0%/*}/temp_xargs" -n 999 -P 8 "${un_bb}" grep -l '^  *[a-z]get [p,v][0-9][0-9]*, L.*/R\$[a-z][a-z]*;->.*:I$')";
local used_array="$("${un_bb}" echo "$used_array"|"${un_bb}" sort|"${un_bb}" uniq)";
for one_of_u in $used_array; do
if [ -z "$job_list_u" ]; then
local job_list_u="${F}=${one_of_u}";
else
local job_list_u="${job_list_u}
${F}=${one_of_u}";
fi
done
local used_array="";
"${un_bb}" echo -n "$job_list_u"|"${un_bb}" tr -s '\n' '\x00'|"${un_bb}" xargs -0 -r -n 1 -P 4 "${0}_part3";
local job_list_u="";
setspan_blue "  Removing unused 'BuildConfig.smali'...";
local b_c_fs="$("${un_bb}" xargs -0 -r -a "${0%/*}/temp_xargs" -n 200 "${un_bb}" echo -n|"${un_se}" 's|  *\./|\n\./|g'|"${un_bb}" grep '/BuildConfig.smali$')";
if [ -z "$b_c_fs" ]; then
local b_c_f="0";
echo '  Found 0 "BuildConfig" classes!';
else
local b_c_f="$("${un_bb}" echo "$b_c_fs"|"${un_bb}" wc -l)";
echo "  Found ${b_c_f} \"BuildConfig\" classes.";
for one_of_b in $b_c_fs; do
if [ -z "$job_list_b" ]; then
local job_list_b="${F}=${one_of_b}";
else
local job_list_b="${job_list_b}
${F}=${one_of_b}";
fi
done
local b_c_fs="";
local del_list_b="$("${un_bb}" echo -n "$job_list_b"|"${un_bb}" tr -s '\n' '\x00'|"${un_bb}" xargs -0 -r -n 1 -P 4 "${0}_part2")"
local job_list_b="";
fi
setspan_blue "  Removing unused 'R*.smali'...";
local r_locations="$("${un_bb}" xargs -0 -r -a "${0%/*}/temp_xargs" -n 200 "${un_bb}" echo -n|"${un_se}" 's|  *\./|\n\./|g'|"${un_bb}" grep '/R$string.smali$')";
if [ -z "$r_locations" ]; then
local r_num=0;
echo '  Found 0 "R" classes paths!';
else
local r_num="$("${un_bb}" echo "$r_locations"|"${un_bb}" wc -l)";
echo "  Found ${r_num} \"R\" classes paths.";
local r_locations="$("${un_bb}" echo -n "$r_locations"|"${un_se}" 's|/[^/][^/]*$||g')";
for r_loc in $r_locations; do
if [ -z "$job_list" ]; then
local job_list="${F}=${r_loc}";
else
local job_list="${job_list}
${F}=${r_loc}";
fi
done
local r_locations="";
local del_list="$("${un_bb}" echo -n "$job_list"|"${un_bb}" tr -s '\n' '\x00'|"${un_bb}" xargs -0 -r -n 1 -P 4 "${0}_part1")";
local job_list="";
"${un_bb}" echo -n "$del_list"|"${un_bb}" tr -s '\n' '\x00'|"${un_bb}" xargs -0 -r -n 999 rm -f
local b_c_f=0;
[ -n "$del_list" ] && local b_c_f="$("${un_bb}" echo "$del_list"|"${un_bb}" wc -l)";
local del_list="";
fi
"${un_bb}" echo -n "$del_list_b"|"${un_bb}" tr -s '\n' '\x00'|"${un_bb}" xargs -0 -r -n 999 rm -f
[ -n "$del_list_b" ] && local b_c_f="$(($("${un_bb}" echo "$del_list_b"|"${un_bb}" wc -l) + $b_c_f))";
local del_list_b="";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
   Files removed: ${b_c_f}
    Removed data: $(($((${s_before} - $("${un_du}" "$F")))/1024)) Kb";
[ -f "${0}_part1" ] && rm -f "${0}_part1";
[ -f "${0}_part2" ] && rm -f "${0}_part2";
[ -f "${0}_part3" ] && rm -f "${0}_part3";
[ -f "${0%/*}/temp_xargs" ] && rm -f "${0%/*}/temp_xargs";
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
[ -f "${0}_part1" ] && rm -f "${0}_part1";
[ -f "${0}_part2" ] && rm -f "${0}_part2";
[ -f "${0}_part3" ] && rm -f "${0}_part3";
[ -f "${0%/*}/temp_xargs" ] && rm -f "${0%/*}/temp_xargs";
exit 0
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
exit 0