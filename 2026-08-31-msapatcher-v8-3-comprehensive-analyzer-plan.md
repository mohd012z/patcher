# MSAPatcher V8.3 Comprehensive Analyzer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an offline, structured APK static-analysis pipeline that feeds meaningful results into Analyze, Evidence, and Report.

**Architecture:** Introduce focused pure-Kotlin analyzers that consume archive entry metadata and bounded text samples, then aggregate their evidence into the existing ScanResult. Android UI continues to use a shared Activity ViewModel; only the scanner touches ContentResolver/ZipInputStream.

**Tech Stack:** Kotlin 2.0.21, AndroidX, Material Components, JUnit 4, Android API 26+, Java 17.

**Spec:** `docs/superpowers/specs/2026-08-31-msapatcher-v8-3-comprehensive-analyzer-design.md`

## Global Constraints
- Static analysis only.
- No APK modification, protection bypass, licensing bypass, signature removal/replacement, runtime hooking, or execution of third-party APK code.
- minSdk 26.
- targetSdk/compileSdk 35.
- Java 17.
- Keep reads bounded; never load an entire large APK into memory.
- Analyzer failure must degrade to LIMITED/ERROR evidence rather than crash the scan.

---

### Task 1: Extend structured evidence and scan models

**Files:**
- Modify: `app/src/main/java/com/msa/patcher/analyze/ScanModels.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/ScanModelsTest.kt`

**Interfaces:**
- Produces: `EvidenceType`, extended `ScanFinding`, extended `ScanResult`

- [ ] Step 1: Write a failing test that constructs a `ScanFinding` with `source` and `evidenceType`, and a `ScanResult` with analyzer coverage fields.
- [ ] Step 2: Run `./gradlew testDebugUnitTest --tests com.msa.patcher.analyze.ScanModelsTest --stacktrace` and confirm compile/test failure because fields do not exist.
- [ ] Step 3: Add:
```kotlin
enum class EvidenceType { INVENTORY, MANIFEST, DEX, RESOURCE, NETWORK, NATIVE, SIGNING, HEURISTIC }

data class ScanFinding(
    val category: String,
    val title: String,
    val detail: String,
    val confidence: String,
    val source: String = "APK",
    val evidenceType: EvidenceType = EvidenceType.HEURISTIC
)
```
Extend `ScanResult` with:
```kotlin
val sampledBytes: Int = 0,
val analyzersRun: Set<String> = emptySet(),
val analyzersLimited: Set<String> = emptySet(),
val warnings: List<String> = emptyList()
```
- [ ] Step 4: Re-run the focused test and confirm PASS.
- [ ] Step 5: Commit with `git commit -m "feat: extend V8.3 scan evidence model"`.

### Task 2: Add network endpoint analyzer

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/NetworkAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/NetworkAnalyzerTest.kt`

**Interfaces:**
- Consumes: `Map<String,String>` source text
- Produces: `fun analyze(textBySource: Map<String,String>): List<ScanFinding>`

- [ ] Step 1: Write tests proving duplicate URLs collapse to one finding and source path is preserved.
- [ ] Step 2: Run the focused test and confirm failure because `NetworkAnalyzer` is missing.
- [ ] Step 3: Implement URL/host extraction using conservative regex, URI parsing where possible, normalization, deduplication, and `SDK & Network` findings.
- [ ] Step 4: Run focused tests and confirm PASS.
- [ ] Step 5: Commit with `git commit -m "feat: add static network endpoint analyzer"`.

### Task 3: Add DEX metadata analyzer

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/DexAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/DexAnalyzerTest.kt`

**Interfaces:**
- Consumes: list of DEX entry names plus bounded printable text by source
- Produces: `fun analyze(dexEntries: List<String>, textBySource: Map<String,String>): List<ScanFinding>`

- [ ] Step 1: Add failing tests for multidex, reflection/ClassLoader, WebView, and package descriptor indicators.
- [ ] Step 2: Verify RED with the focused Gradle test.
- [ ] Step 3: Implement metadata/string analysis without decompilation.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: add DEX metadata analyzer"`.

### Task 4: Add native/JNI analyzer

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/NativeAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/NativeAnalyzerTest.kt`

**Interfaces:**
- Consumes: APK entry names and bounded `.so` printable strings
- Produces: `fun analyze(entries: List<String>, textBySource: Map<String,String>): List<ScanFinding>`

- [ ] Step 1: Add failing tests for ABI extraction and JNI/native library evidence.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Implement ABI/library grouping and static native markers.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: add native JNI analyzer"`.

### Task 5: Add resources/config analyzer

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/ResourceAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/ResourceAnalyzerTest.kt`

**Interfaces:**
- Consumes: archive entries and bounded text map
- Produces: `fun analyze(entries: List<String>, textBySource: Map<String,String>): List<ScanFinding>`

- [ ] Step 1: Add failing tests for assets/config counts and Flutter/Unity/React Native archive markers.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Implement structured resource findings.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: add resource configuration analyzer"`.

### Task 6: Add manifest analyzer with safe fallback

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/ManifestAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/ManifestAnalyzerTest.kt`

**Interfaces:**
- Consumes: bounded manifest bytes/text
- Produces: `data class ManifestAnalysis(val findings: List<ScanFinding>, val limited: Boolean, val warning: String?)`

- [ ] Step 1: Add failing tests for plaintext manifest parsing and binary/unreadable fallback.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Implement safe plaintext XML extraction first; for binary AXML return explicit LIMITED result rather than inventing fields.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: add safe manifest analyzer"`.

### Task 7: Add signing/integrity inventory analyzer

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/SigningAnalyzer.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/SigningAnalyzerTest.kt`

**Interfaces:**
- Consumes: archive entry names
- Produces: `fun analyze(entries: List<String>): List<ScanFinding>`

- [ ] Step 1: Add failing tests for META-INF `.RSA`, `.DSA`, `.EC`, `.SF`, and `MANIFEST.MF` discovery.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Implement signing-artifact inventory only.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: add signing artifact inventory"`.

### Task 8: Refactor StaticApkScanner into analyzer pipeline

**Files:**
- Modify: `app/src/main/java/com/msa/patcher/analyze/StaticApkScanner.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/AnalyzerAggregationTest.kt`

**Interfaces:**
- Consumes: all analyzer functions from Tasks 2-7
- Produces: populated `ScanResult`

- [ ] Step 1: Add a failing aggregation test using synthetic entries/text maps to prove findings from multiple analyzers are combined and analyzersRun/limited are tracked.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Refactor scanner so one bounded ZIP pass collects entry names and selected source samples, then calls analyzers.
- [ ] Step 4: Preserve Quick/Deep budgets; Deep broadens sampled extensions and per-entry coverage.
- [ ] Step 5: Verify focused aggregation test and all analyzer tests.
- [ ] Step 6: Commit with `git commit -m "feat: integrate V8.3 analyzer pipeline"`.

### Task 9: Upgrade Analyze category detail mapping

**Files:**
- Modify: `app/src/main/java/com/msa/patcher/analyze/AnalysisStateMapper.kt`
- Modify: `app/src/main/java/com/msa/patcher/analyze/AnalyzeFragment.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/AnalysisStateMapperTest.kt`

**Interfaces:**
- Consumes: structured `ScanResult.findings`
- Produces: category states and source-aware detail text

- [ ] Step 1: Extend tests for FOUND/CLEAN/READY/LIMITED/ERROR behavior and source display.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Map analyzer coverage to category state; render source and confidence in category detail.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: show comprehensive Analyze evidence"`.

### Task 10: Upgrade Evidence tab

**Files:**
- Modify: `app/src/main/java/com/msa/patcher/evidence/EvidenceFragment.kt`
- Test: `app/src/test/java/com/msa/patcher/analyze/EvidenceFormatterTest.kt`
- Create: `app/src/main/java/com/msa/patcher/evidence/EvidenceFormatter.kt`

**Interfaces:**
- Consumes: `List<ScanFinding>`
- Produces: source-aware readable evidence output

- [ ] Step 1: Add failing formatter tests for category, confidence, source, and detail.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Implement pure formatter and wire fragment to it.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: show source-aware evidence details"`.

### Task 11: Upgrade Report tab and V8.3 identity

**Files:**
- Modify: `app/src/main/java/com/msa/patcher/report/ReportFragment.kt`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/com/msa/patcher/AppIdentityTest.kt`
- Test: `app/src/test/java/com/msa/patcher/report/ReportSummaryTest.kt`
- Create: `app/src/main/java/com/msa/patcher/report/ReportSummary.kt`

**Interfaces:**
- Consumes: `ScanResult`
- Produces: report text and V8.3 build identity

- [ ] Step 1: Add failing tests for analyzer counts, evidence counts, warnings, and version `8.3`.
- [ ] Step 2: Verify RED.
- [ ] Step 3: Add `ReportSummary`, wire ReportFragment, set `versionCode = 83`, `versionName = "8.3"`.
- [ ] Step 4: Verify GREEN.
- [ ] Step 5: Commit with `git commit -m "feat: complete V8.3 report and version"`.

### Task 12: Full regression and Android build verification

**Files:**
- Modify if needed: `.github/workflows/android-build.yml` artifact label only

**Interfaces:**
- Produces: verified V8.3 debug APK artifact

- [ ] Step 1: Run `./gradlew testDebugUnitTest --stacktrace`.
- [ ] Step 2: Confirm zero failed tests.
- [ ] Step 3: Run `./gradlew assembleDebug --stacktrace`.
- [ ] Step 4: Confirm `app/build/outputs/apk/debug/app-debug.apk` exists.
- [ ] Step 5: Run SHA-256 verification on the APK.
- [ ] Step 6: Update workflow artifact name from `MSAPatcher-8.0-debug` to `MSAPatcher-8.3-debug`.
- [ ] Step 7: Push and verify GitHub Actions steps Unit tests, Build debug APK, Verify APK, and Upload APK all succeed.
