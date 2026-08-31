main_func() {
local lang_leave="ru";
setspan_green "
  Dellang working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_du="${util_dir}/mdu";
local F="$(pwd)";
local F="${F%/res}";
local start_at="$(date +%H:%M:%S)";
local arg_arr="0";
local s_before="$("${un_du}" "$(pwd)")";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}";
for l_leave in $lang_leave; do
if [ -d "values-${l_leave}" ]; then
"${un_bb}" mv -f values-${l_leave} dl_tmp_values-${l_leave};
fi
for l_leave_reg in $(echo values-${l_leave}-*); do
if [ -d "$l_leave_reg" ]; then
"${un_bb}" mv -f ./${l_leave_reg} ./dl_tmp_${l_leave_reg};
fi
done
done
for FFF in $(echo values-[a-z]); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z]+*); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z][a-z]); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z][a-z]); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z][a-z]-*); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z][a-z][a-z]); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for FFF in $(echo values-[a-z][a-z][a-z]-*); do
if [ -d "$FFF" ];then
local arg_arr="$((${arg_arr}+1))";
"${un_bb}" rm -rf "./${FFF}";
fi
done
for l_restore in $(echo dl_tmp_values-*); do
if [ -d "$l_restore" ]; then
"${un_bb}" mv -f ./${l_restore} ./${l_restore#dl_tmp_};
fi
done
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
  Lang's removed: ${arg_arr}
   Removed files: $(($((${s_before}-$("${un_du}" "$(pwd)")))/1024)) Kb";
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
[ -d "$@" ] || ab_ort "Object
    \"${@}\"
  is not a directory!";
cd "${@}/res" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${@}/res\"!";
main_func
cd - >/dev/null 2>&1;
exit
