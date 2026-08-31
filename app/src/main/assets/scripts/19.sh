main_func() {
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_ads="${util_dir}/CrashLogger";
[ -f "$un_ads" ] || ab_ort "\"CrashLogger\" binary is not installed!
   Please install it before using this patch.";
[ -f "${un_ads}.jar" ] || ab_ort "\"CrashLogger\" binary is not installed!
   Please install it before using this patch.";
otkat() {
setspan_blue "  Removing CrashLogger...";
local sup_name="$("${un_bb}" grep "^\.super " ./smali*/ru/maximoff/crash/App.smali|\
"${un_bb}" head -n 1|\
"${un_bb}" sed 's|^.* L\(.*\).$|\1|g'|\
"${un_bb}" sed 's|/|\.|g')";
[ -z "$sup_name" ] || [ "$sup_name" = "android.app.Application" ] || "${un_bb}" sed -i "s|ru\.maximoff\.crash\.App|$sup_name|g" ./AndroidManifest.xml;
"${un_bb}" sed -i 's|android:name=\"ru\.maximoff\.crash\.App\"||g' ./AndroidManifest.xml;
echo "$("${un_bb}" cat ./AndroidManifest.xml|\
"${un_bb}" sed 's|.*|\0<<<<EnDOfLiNe<<<<|g'|\
"${un_bb}" tr -d '\n'|\
"${un_bb}" sed 's|<activity[^>][^>]*android:name=\"ru\.maximoff\.crash\.Act[^>][^>]*>||g'|\
"${un_bb}" sed 's|<<<<EnDOfLiNe<<<<|\n|g'|\
"${un_bb}" sed '/^  *$/d'|\
"${un_bb}" sed '/^$/d'|\
"${un_bb}" sed 's|\([^=]\"\)  \([a-z]\)|\1 \2|g')">./AndroidManifest.xml;
"${un_bb}" rm -rf ./smali*/ru/maximoff/crash 2>/dev/null;
"${un_bb}" rmdir ./smali*/ru/maximoff 2>/dev/null;
"${un_bb}" rmdir ./smali*/ru 2>/dev/null;
"${un_bb}" sed -i '/<style  *name=\"ru_maximoff_CrashLoggerTheme/,/<\/style>/d' ./res/values*/styles.xml 2>/dev/null;
"${un_bb}" sed -i '/<public  *type=\"style\"  *name=\"ru_maximoff_CrashLoggerTheme/d' ./res/values*/public.xml 2>/dev/null;
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
exit 0
}
[ -f "${0%/*}/bin/utils" ] && { . "${0%/*}/bin/utils"; } || setspan_green() {
echo "$@";
}
setspan_green "
  CrashLogger working...
";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}
";
[ -z "$("${un_bb}" grep "android:name=\"ru.maximoff.crash.A" ./AndroidManifest.xml 2>/dev/null)" ] || otkat;
[ -d "$(echo ./smali*/ru/maximoff/crash)" ] && otkat;
[ -z "$("${un_bb}" grep "ru_maximoff_CrashLoggerTheme" ./res/values*/styles.xml 2>/dev/null)" ] || otkat;
setspan_blue "  Patching files of the current project...
";
"${un_ads}" "$F";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
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
[ -d "${@}/smali" ] || ab_ort "Directory
    \"${@}/smali\"
  is not exist!
  You must decompile package to smali!";
[ -d "${@}/res" ] || ab_ort "Directory
    \"${@}/res\"
  is not exist!
  You must decompile package to resources!";
cd "$@" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${@}\"!";
main_func;
cd - >/dev/null 2>&1;
exit