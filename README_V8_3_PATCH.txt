MSAPatcher V8.3 Comprehensive Analyzer Patch

Purpose
- Replace the lightweight string-only Analyze flow with a structured offline APK static-analysis pipeline.
- Read-only analysis only. This patch does not modify APKs, bypass licensing/protection, replace signatures, hook apps, or execute selected APK code.

Major additions
1. Structured evidence with source path and evidence type.
2. AndroidManifest analyzer with plaintext XML and binary AXML parsing.
3. DEX header metadata: string/type/proto/field/method/class counts.
4. Network URL/host extraction with deduplication and source tracking.
5. Native/JNI ABI/library inventory and static symbol/string evidence.
6. Resources/config/framework inventory.
7. Signing artifact inventory with explicit v2/v3/v4 limitation.
8. Protection/obfuscation static markers.
9. Source-aware Analyze category detail.
10. Source-aware Evidence output.
11. Dynamic Report coverage based on analyzers run/limited.
12. Version 8.3 / code 83 and GitHub artifact name MSAPatcher-8.3-debug.

Apply
Extract this archive OVER the root of your MSAPatcher project so `app/` replaces the existing root `app/` files. Do not keep this patch as a nested folder inside the project.

Verification already performed locally
- Pure Kotlin analyzer regression: PASS.
- Synthetic APK end-to-end scanner pipeline with Android stubs: PASS.
- StaticApkScanner Kotlin compile with Android stubs: PASS.

Not yet verified
- Full Android Gradle build and GitHub Actions. After applying and pushing, GitHub Actions must pass Unit tests, Build debug APK, Verify APK, and Upload APK before the V8.3 APK is considered build-verified.
