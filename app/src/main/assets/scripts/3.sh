main_func1() {
local glob_patt_dpi="$@";
setspan_green "
  Drawableclean working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_du="${util_dir}/mdu";
local start_at="$(date +%H:%M:%S)";
local f_name="$(pwd)"; local f_name="${f_name%/res}";
local f_name="${f_name##*/}";
echo "
  App: ${f_name%_src}
  DPI: ${drawableclean_user_dpi}dpi
";
local s_before="$("${un_du}" "$(pwd)")";
if [ "$glob_patt_dpi" = "l" ]; then
main_func2 l;
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxxh;
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxh;
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xh;
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 h;
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 m;
elif [ "$glob_patt_dpi" = "m" ]; then
main_func2 m;
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 l;
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxxh;
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxh;
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xh;
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 h;
elif [ "$glob_patt_dpi" = "h" ]; then
main_func2 h;
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 m;
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 l;
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxxh;
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxh;
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xh;
elif [ "$glob_patt_dpi" = "xh" ]; then
main_func2 xh;
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 h;
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 m;
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 l;
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxxh;
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxh;
elif [ "$glob_patt_dpi" = "xxh" ]; then
main_func2 xxh;
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xh;
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 h;
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 m;
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 l;
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxxh;
elif [ "$glob_patt_dpi" = "xxxh" ]; then
main_func2 xxxh;
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xxh;
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 xh;
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 h;
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 m;
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && main_func2 l;
else
ab_ort "Unknown DPI pattern!";
fi
setspan_blue "  Removing empty dir's:"
"${un_bb}" find . -empty -type d -delete -print
setspan_blue "
  Done!
";
setspan_green "  Results:"
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
   Removed files: $(($((${s_before} - $("${un_du}" "$(pwd)")))/1024)) Kb";
}
main_func2() {
local draw_leave="-${1}dpi";
for bas_e in $(echo drawable*${draw_leave}*); do
if [ -d "$bas_e" ]; then
"${un_bb}" mv -f "$bas_e" dr_cl_${bas_e} || ab_ort "Can not rename directory!";
fi
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.png"); do
{ "${un_bb}" find drawable* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.webp"); do
{ "${un_bb}" find drawable* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.jpg"); do
{ "${un_bb}" find drawable* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.jpeg"); do
{ "${un_bb}" find drawable* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.gif"); do
{ "${un_bb}" find drawable* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for bas_e in $(echo dr_cl_*); do
if [ -d "$bas_e" ]; then
"${un_bb}" mv -f "$bas_e" ${bas_e#dr_cl_} || echo -e "\n  ERROR!\nCan not rename directory!\n";
fi
done
if [ -d "$(echo mipmap*${draw_leave}* |"${un_bb}" awk '{print $1}')" ];then
for bas_e in $(echo mipmap*${draw_leave}*); do
if [ -d "$bas_e" ]; then
"${un_bb}" mv -f "$bas_e" dr_cl_${bas_e} || ab_ort "Can not rename directory!";
fi
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.png"); do
{ "${un_bb}" find mipmap* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for each_f in $("${un_bb}" find $(echo dr_cl_*) -type f -iname "*.xml"); do
{ "${un_bb}" find mipmap* -type f -name "${each_f##*/}" |"${un_bb}" xargs -n 1 -P 8 "${un_bb}" rm -f; } 2>/dev/null;
done
for bas_e in $(echo dr_cl_*); do
if [ -d "$bas_e" ]; then
"${un_bb}" mv -f "$bas_e" ${bas_e#dr_cl_} || echo -e "\n  ERROR!\nCan not rename directory!\n";
fi
done
fi
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
ab_ort2() {
setspan_red "
  No \"drawable-*dpi\" directories found!
  No objects to be processed!
";
exit
}
ex_it() {
cd - >/dev/null 2>&1;
exit
}
[ -z "$(echo -n "$@")" ] && ab_ort "No arguments passed!";
[ -e "$1" ] || ab_ort "Object
    \"${1}\"
  does not exist!";
[ -d "$1" ] || ab_ort "Object
    \"${1}\"
  is not a directory!";
cd "${1}/res" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
    \"${1}/res\"!";
xxxhdpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
ab_ort2
}
xxhdpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
ab_ort2
}
xhdpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
ab_ort2
}
hdpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
ab_ort2
}
mdpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
ab_ort2
}
ldpi_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
[ -d "$(echo drawable-ldpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 l; ex_it; };
[ -d "$(echo drawable-mdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 m; ex_it; };
[ -d "$(echo drawable-hdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 h; ex_it; };
[ -d "$(echo drawable-xhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xh; ex_it; };
[ -d "$(echo drawable-xxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxh; ex_it; };
[ -d "$(echo drawable-xxxhdpi* |"${un_bb}" awk '{print $1}')" ] && { main_func1 xxxh; ex_it; };
ab_ort2
}
sel_f() {
local user_choise="$@";
case $user_choise in
l) export drawableclean_user_dpi="l"; ldpi_func;;
m) export drawableclean_user_dpi="m"; mdpi_func;;
h) export drawableclean_user_dpi="h"; hdpi_func;;
xh) export drawableclean_user_dpi="xh"; xhdpi_func;;
xxh) export drawableclean_user_dpi="xxh"; xxhdpi_func;;
xxxh) export drawableclean_user_dpi="xxxh"; xxxhdpi_func;;
"") echo "
..The default settings are used..
"; export drawableclean_user_dpi="xxh"; xxhdpi_func;;
*) ab_ort "Wrong key!
    \"$user_choise\"";;
esac
}
sel_f "$2"
ex_it