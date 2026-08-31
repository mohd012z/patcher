main_func() {
local tar_g="$(pwd)";
setspan_green "
  PngOptimizer working...
";
echo "  Target directory:
\"${tar_g}\"
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_du="${util_dir}/mdu";
local un_pn="${util_dir}/pngquant";
local un_op="${util_dir}/optipng";
for F in optipng pngquant; do
[ -f "${util_dir}/${F}" ] || ab_ort "\"${F}\"
  is not installed!";
done
local start_at="$(date +%H:%M:%S)";
local s_before="$("${un_du}" "$tar_g")";
setspan_blue "  Searching for png...";
local vic_arr="$("${un_bb}" find . -type f -iname "*.png" 2>/dev/null |"${un_bb}" sed '/^\.\/build\//d
/^\.\/original\//d
/^\.\/smali.*\//d
/^\.\/kotlin.*\//d
/.*\.9\.png$/d
/^$/d')";
if [ -n "$vic_arr" ]; then
local victimpng_arr="$(echo "$vic_arr"|"${un_bb}" wc -l)";
setspan_blue "  Optimizing colors..."
echo -n $vic_arr |"${un_bb}" xargs -n 1 -P 8 "${un_pn}" 64 --force --strip --skip-if-larger --ext .png
setspan_blue "  Optimizing the structure..."
echo -n $vic_arr |"${un_bb}" xargs -n 1 -P 8 "${un_op}" -o7 -preserve -silent -clobber
else
local victimpng_arr="0";
echo "  No png files found!";
fi

setspan_blue "
  Searching for 9.png...";
local vic_arr9="$("${un_bb}" find . -type f -iname "*.9.png" 2>/dev/null |"${un_bb}" sed '/^\.\/build\//d
/^\.\/original\//d
/^\.\/smali.*\//d
/^\.\/kotlin.*\//d
/^$/d')";
if [ -n "$vic_arr9" ]; then
local victimpng9_arr="$(echo "$vic_arr9"|"${un_bb}" wc -l)";
setspan_blue "  Optimizing colors..."
echo -n $vic_arr9 |"${un_bb}" xargs -n 1 -P 8 "${un_pn}" 64 --force --strip --skip-if-larger --ext .png
else
local victimpng9_arr="0";
echo "  No 9.png files found!";
fi
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)
 Files processed:
             png| ${victimpng_arr}
           9.png| ${victimpng9_arr}
           Freed: $(($((${s_before}-$("${un_du}" "$tar_g")))/1024)) Kb";
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
cd "$@" 2>/dev/null || ab_ort "Cannot \"cd\" to
\"${@}\"!";
main_func
exit