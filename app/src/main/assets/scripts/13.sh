main_func() {
local targ_name="$@";
setspan_green "
  Clone hard working...
";
local util_dir="${0%/*}/bin";
local un_bb="${util_dir}/busybox";
local un_se="${util_dir}/sed";
local F="$(pwd)";
local start_at="$(date +%H:%M:%S)";
local f_name="${F##*/}";
echo "  App: ${f_name%_src}
";
local pkg_name="$("${un_bb}" grep " package=\".*\"" "./AndroidManifest.xml" 2>/dev/null |"${un_bb}" head -n1 2>/dev/null |"${un_se}" 's|.* package=\"\([^\"][^\"]*\)\".*|\1|' 2>/dev/null)";
[ -z "$pkg_name" ] && ab_ort "Can't get package name from \"AndroidManifest.xml\"!
  Please report about this bug!";
echo "  Current package name is:
    \"${pkg_name}\"";
[ -z "$targ_name" ] && {
setspan_blue "  No new package name passed!
  Autogenerate...";
local short_pkg="$(echo "$pkg_name" |"${un_se}" 's|\(.*\).|\1|g
/^$/d' 2>/dev/null)";
while true; do
local targ_name="${short_pkg}$("${un_bb}" tr -dc 'a-z' < /dev/urandom |"${un_bb}" dd bs=1 count=1 2>/dev/null)";
[ "$targ_name" = "$pkg_name" ] || break;
done
}
echo "  New package name is:
    \"${targ_name}\"";
[ "$pkg_name" = "$targ_name" ] && ab_ort "Current package name = target package name!
  Nothing to handle!";
local old_yml="";
local old_json="";
[ -f "apktool.yml" ] && local old_yml="$("${un_bb}" cat apktool.yml 2>/dev/null)";
[ -f "apktool.json" ] && local old_json="$("${un_bb}" cat apktool.json 2>/dev/null)";
setspan_blue "  Patching files of the current project...";
{ "${un_bb}" find . -type f -print0|"${un_bb}" xargs -0 -r -n 999 -P 4 "${un_se}" -i "s|${pkg_name//./\\.}|${targ_name//./\\.}|g
s|${pkg_name//.//}|${targ_name//.//}|g" ; } 2>/dev/null
if [ -e "$(echo ./smali*/${pkg_name//.//} |"${un_bb}" awk '{print $1}')" ]; then
setspan_blue "  Rebuilding smali...";
for sm_f in $(echo ./smali*); do
[ -d "${sm_f}/${pkg_name//.//}" ] && {
[ -d "${sm_f}/${targ_name//.//}" ] || "${un_bb}" mkdir -p "${sm_f}/${targ_name//.//}";
"${un_bb}" mv -f "${sm_f}/${pkg_name//.//}"/* "${sm_f}/${targ_name//.//}" 2>&1;
};
[ -f "${sm_f}/${pkg_name//.//}.smali" ] && {
local targdir_name="${targ_name//.//}";
local targdir_name="${targdir_name%/*}";
local pkgdir_name="${pkg_name//.//}";
local targdir_name="${pkgdir_name%/*}";
"${un_bb}" mv -f "${sm_f}/${pkg_name//.//}.smali" "${sm_f}/${pkgdir_name}/${targ_name##*.}.smali";
[ -d "${sm_f}/${targdir_name}" ] || "${un_bb}" mkdir -p "${sm_f}/${targdir_name}";
"${un_bb}" mv -f "${sm_f}/${pkgdir_name}"/* "${sm_f}/${targdir_name}";
};
done
"${un_bb}" find ./smali*/ -empty -type d -delete
fi
[ -n "$old_yml" ] && echo "$old_yml" >./apktool.yml;
[ -n "$old_json" ] && echo "$old_json" >./apktool.json;
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
[ -z "$(echo -n "$@")" ] && ab_ort "No arguments passed!";
[ -e "$1" ] || ab_ort "Object
    \"${1}\"
  does not exist!";
[ -d "$1" ] || ab_ort "Object
    \"${1}\"
  is not a directory!";
[ -d "${1}/res" ] || ab_ort "Directory
    \"${1}/res\"
  is not exist!
  You must decompile package resources!";
[ -d "${1}/smali" ] || ab_ort "Directory
    \"${1}/smali\"
  is not exist!
  You must decompile package to smali!";
cd "$1" >/dev/null 2>&1 || ab_ort "Can not 'cd' to
\"${1}\"!";
main_func "$2";
cd - >/dev/null 2>&1;
exit