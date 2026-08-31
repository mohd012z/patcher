#!/system/bin/sh
setspan_green(){ echo "$@"; }
setspan_blue(){ echo "$@"; }
setspan_red(){ echo "$@"; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }
[ -n "$1" ] || ab_ort "No project directory passed!"
[ -d "$1" ] || ab_ort "Project directory does not exist: $1"
cd "$1" || ab_ort "Cannot enter project directory"
TS=$(date +%Y%m%d_%H%M%S 2>/dev/null)
[ -n "$TS" ] || TS="snapshot"
DEST="MSAPatcher_Snapshot_$TS"
mkdir -p "$DEST" || ab_ort "Cannot create snapshot directory"
[ -f AndroidManifest.xml ] && cp -p AndroidManifest.xml "$DEST/"
for d in smali* res assets lib; do [ -e "$d" ] && cp -pr "$d" "$DEST/"; done
setspan_green "\n  Snapshot created."
setspan_blue "  Location: $(pwd)/$DEST\n"
exit 0
