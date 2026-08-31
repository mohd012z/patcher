#!/system/bin/sh
setspan_green(){ echo "$@"; }
setspan_blue(){ echo "$@"; }
setspan_red(){ echo "$@"; }
ab_ort(){ setspan_red "\n  $*\n"; exit 1; }
[ -n "$1" ] || ab_ort "No project directory passed!"
[ -d "$1" ] || ab_ort "Project directory does not exist: $1"
cd "$1" || ab_ort "Cannot enter project directory"
OUT="MSAPatcher_RiskCheck.txt"
smali_dirs=$(find . -maxdepth 1 -type d -name 'smali*' 2>/dev/null | wc -l | tr -d ' ')
smali_files=$(find smali* -type f -name '*.smali' 2>/dev/null | wc -l | tr -d ' ')
so_files=$(find lib -type f -name '*.so' 2>/dev/null | wc -l | tr -d ' ')
reflection=$(grep -R -I -E 'Class;->forName|java/lang/reflect|DexClassLoader|PathClassLoader' smali* 2>/dev/null | wc -l | tr -d ' ')
sigchecks=$(grep -R -I -E 'GET_SIGNATURES|GET_SIGNING_CERTIFICATES|SigningInfo|MessageDigest|X509Certificate|checkSignatures' smali* AndroidManifest.xml 2>/dev/null | wc -l | tr -d ' ')
providers=$(grep -o '<provider ' AndroidManifest.xml 2>/dev/null | wc -l | tr -d ' ')
services=$(grep -o '<service ' AndroidManifest.xml 2>/dev/null | wc -l | tr -d ' ')
receivers=$(grep -o '<receiver ' AndroidManifest.xml 2>/dev/null | wc -l | tr -d ' ')
{
 echo "MSAPatcher 6 - Patch Risk Check"
 echo "Project: $(pwd)"
 echo "Generated: $(date 2>/dev/null)"
 echo ""
 echo "Smali directories: $smali_dirs"
 echo "Smali files:       $smali_files"
 echo "Native .so files:  $so_files"
 echo "Reflection/dynamic-loader references: $reflection"
 echo "Signature-check indicators:           $sigchecks"
 echo "Manifest providers/services/receivers: $providers / $services / $receivers"
 echo ""
 echo "== Risk Guidance =="
 [ "$so_files" -gt 0 ] 2>/dev/null && echo "MEDIUM: Native libraries exist; Java/smali-only changes may not cover all behavior."
 [ "$reflection" -gt 20 ] 2>/dev/null && echo "HIGH: Heavy reflection/dynamic loading detected; deleting classes can cause runtime crashes."
 [ "$sigchecks" -gt 0 ] 2>/dev/null && echo "HIGH: Signature/self-integrity related code detected; re-signing may change application behavior."
 [ "$smali_dirs" -gt 1 ] 2>/dev/null && echo "INFO: Multi-dex project. Any dependency scan should include every smali_classes* directory."
 echo ""
 echo "This checker does not modify the project."
} > "$OUT"
setspan_green "\n  Risk check complete."
setspan_blue "  Report: $(pwd)/$OUT\n"
cat "$OUT"
exit 0
