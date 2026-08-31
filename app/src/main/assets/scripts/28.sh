#!/system/bin/sh
setspan_green(){ echo "$@"; }
setspan_blue(){ echo "$@"; }
setspan_red(){ echo "$@"; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }
[ -n "$1" ] || ab_ort "No project directory passed!"
[ -d "$1" ] || ab_ort "Project directory does not exist: $1"
cd "$1" || ab_ort "Cannot enter project directory"
OUT="MSAPatcher_NetworkAudit.txt"
RAW=".mpatcher_domains.raw"
: > "$OUT"; : > "$RAW"
# Extract URL-like domains conservatively from text resources/smali.
grep -R -I -E -o 'https?://[A-Za-z0-9._-]+|[A-Za-z0-9_-]+\.(com|net|io|org|co|app|dev)(/[A-Za-z0-9._~:/?#\[\]@!$&()*+,;=%-]*)?' smali* res assets AndroidManifest.xml 2>/dev/null \
 | sed -E 's#.*https?://##; s#/.*##; s#^[^:]+:##' \
 | tr '[:upper:]' '[:lower:]' | sort -u > "$RAW"
{
 echo "MSAPatcher 6 - Network Domain Audit"
 echo "Project: $(pwd)"
 echo "Generated: $(date 2>/dev/null)"
 echo ""
 echo "== Likely Advertising =="
 grep -E 'doubleclick|googlesyndication|googleads|applovin|pangle|bytedance|moloco|vungle|unityads|inmobi|ironsource|chartboost|mintegral|mbridge|facebook.*ads|bigo' "$RAW" || true
 echo ""
 echo "== Likely Analytics / Attribution =="
 grep -E 'appsflyer|firebase|analytics|adjust|branch|kochava|singular|amplitude|mixpanel|crashlytics|sentry' "$RAW" || true
 echo ""
 echo "== Other / Application / Unknown =="
 grep -Ev 'doubleclick|googlesyndication|googleads|applovin|pangle|bytedance|moloco|vungle|unityads|inmobi|ironsource|chartboost|mintegral|mbridge|facebook.*ads|bigo|appsflyer|firebase|analytics|adjust|branch|kochava|singular|amplitude|mixpanel|crashlytics|sentry' "$RAW" || true
 echo ""
 echo "WARNING: Classification is heuristic. Do not block unknown domains blindly; they may provide login, content, updates or core APIs."
} > "$OUT"
rm -f "$RAW"
setspan_green "\n  Network audit complete."
setspan_blue "  Report: $(pwd)/$OUT\n"
cat "$OUT"
exit 0
