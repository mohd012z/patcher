# MSAPatcher V8 Native UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy script-list front-end with a modern native guided-analysis UI while preserving the read-only analysis engine and user scripts.

**Architecture:** Create a new Android front-end shell with five destinations (Home, Analyze, Evidence, Report, Tools), backed by a normalized script registry and a versioned built-in-script migration layer. Existing analysis scripts are imported as engine assets and invoked only through analysis-oriented flows; legacy utilities stay isolated under Tools > Legacy.

**Tech Stack:** Android Gradle project, Kotlin, AndroidX AppCompat/Fragment/RecyclerView, Material Components, existing shell-script assets, JVM unit tests plus Android instrumentation smoke tests.

**Spec:** `docs/superpowers/specs/2026-08-31-msapatcher-v8-native-ui-design.md`

## Global Constraints
- App name: MSAPatcher
- Version name: 8.0
- Version code: 80
- Primary subtitle: Android Intelligence Lab
- Five primary destinations: Home, Analyze, Evidence, Report, Tools
- Built-in script updates must never delete user-created scripts.
- Guided UI remains analysis/read-only and must not add third-party license, billing, signature, or protection-bypass workflows.

---

### Task 1: Bootstrap V8 Application Identity and Navigation Shell

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/msa/patcher/MainActivity.kt`
- Create: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/menu/main_bottom_nav.xml`
- Create: `app/src/test/java/com/msa/patcher/AppIdentityTest.kt`

**Interfaces:**
- Produces: `MainActivity` hosting destinations with IDs `nav_home`, `nav_analyze`, `nav_evidence`, `nav_report`, `nav_tools`.

- [ ] Write `AppIdentityTest` that asserts BuildConfig version is `8.0` and manifest label is `MSAPatcher`.
- [ ] Run `./gradlew testDebugUnitTest` and verify the test fails before V8 configuration exists.
- [ ] Add Gradle configuration with `versionCode = 80`, `versionName = "8.0"`, namespace/applicationId `com.msa.patcher`.
- [ ] Add `MainActivity` and Material bottom navigation with all five destination IDs.
- [ ] Run unit tests and `./gradlew assembleDebug`; both must pass.
- [ ] Commit with `feat: bootstrap MSAPatcher V8 navigation shell`.

### Task 2: Script Registry and Built-in Bundle Migration

**Files:**
- Create: `app/src/main/java/com/msa/patcher/scripts/ScriptDefinition.kt`
- Create: `app/src/main/java/com/msa/patcher/scripts/ScriptRegistry.kt`
- Create: `app/src/main/java/com/msa/patcher/scripts/BuiltinScriptMigrator.kt`
- Create: `app/src/main/assets/scripts/` (import read-only analysis scripts)
- Create: `app/src/main/assets/scripts/index.json`
- Create: `app/src/test/java/com/msa/patcher/scripts/BuiltinScriptMigratorTest.kt`

**Interfaces:**
- Produces: `ScriptDefinition(id: String, title: String, category: String, description: String, mode: String, builtIn: Boolean)`.
- Produces: `BuiltinScriptMigrator.sync(bundleVersion: Int, targetDir: File): MigrationResult`.

- [ ] Write tests proving a new bundle copies built-ins, same bundle does nothing, and a newer bundle refreshes only built-ins while preserving a user script.
- [ ] Run the migration tests and confirm they fail because the migrator is absent.
- [ ] Implement `ScriptDefinition`, registry parsing from `index.json`, and bundle migration using a marker file `.msa_builtin_bundle_version`.
- [ ] Import analysis-focused scripts and categorize them into the 12 analysis groups.
- [ ] Run tests and confirm user-created files survive migration.
- [ ] Commit with `feat: add versioned script registry migration`.

### Task 3: Home Dashboard

**Files:**
- Create: `app/src/main/java/com/msa/patcher/home/HomeFragment.kt`
- Create: `app/src/main/java/com/msa/patcher/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/msa/patcher/model/ApkSummary.kt`
- Create: `app/src/main/res/layout/fragment_home.xml`
- Create: `app/src/main/res/layout/item_capability_card.xml`
- Create: `app/src/test/java/com/msa/patcher/home/HomeViewModelTest.kt`

**Interfaces:**
- Produces: `ApkSummary(fileName, sizeBytes, sha256, dexCount, nativeCount, abis)`.
- Produces: `HomeViewModel.selectApk(uri: Uri)` and `HomeViewModel.summary`.

- [ ] Write a failing test for summary formatting and empty-state behavior.
- [ ] Run the test and verify expected failure.
- [ ] Implement Home with APK selector, Quick Scan, Deep Scan, coverage meter, behaviour meter, and capability-card grid.
- [ ] Ensure no APK selected state contains no stale previous scan data.
- [ ] Run unit tests and Android resource build.
- [ ] Commit with `feat: add MSAPatcher V8 home dashboard`.

### Task 4: APK Precheck and Architecture Classifier Flow

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/PrecheckAnalyzer.kt`
- Create: `app/src/main/java/com/msa/patcher/analyze/ArchitectureClassifier.kt`
- Create: `app/src/main/java/com/msa/patcher/analyze/AnalysisPlan.kt`
- Create: `app/src/test/java/com/msa/patcher/analyze/ArchitectureClassifierTest.kt`

**Interfaces:**
- Produces: `PrecheckResult` with ZIP status, size, DEX count, native count, ABIs, embedded payload counts, signing presence.
- Produces: `ArchitectureClassifier.classify(PrecheckResult, EvidenceSet): ArchitectureFamily`.
- Produces: `AnalysisPlan.quick()` and `AnalysisPlan.deep(ArchitectureFamily)`.

- [ ] Add failing classifier tests for Memory/Process, APK Engineering, Native Hook/ART, Flutter/AOT, and Hybrid fixtures.
- [ ] Run tests and confirm failures.
- [ ] Implement precheck and architecture rules using exact evidence before generic hits.
- [ ] Implement Quick and Deep analysis-plan selection.
- [ ] Run all classifier tests.
- [ ] Commit with `feat: add guided architecture analysis planning`.

### Task 5: Analyze Destination with 12 Categories

**Files:**
- Create: `app/src/main/java/com/msa/patcher/analyze/AnalyzeFragment.kt`
- Create: `app/src/main/java/com/msa/patcher/analyze/CategoryAdapter.kt`
- Create: `app/src/main/res/layout/fragment_analyze.xml`
- Create: `app/src/main/res/layout/item_analysis_category.xml`
- Create: `app/src/androidTest/java/com/msa/patcher/AnalyzeNavigationTest.kt`

**Interfaces:**
- Consumes: `AnalysisPlan` from Task 4 and `ScriptRegistry` from Task 2.
- Produces: category selection and execution state for the 12 guided categories.

- [ ] Add an instrumentation test that opens Analyze and verifies all 12 category labels in order.
- [ ] Run instrumentation test and verify it fails before the screen exists.
- [ ] Implement category cards with states `Not Run`, `Selected`, `Running`, `Complete`, `Skipped`.
- [ ] Make Deep Scan show the selected category plan before execution.
- [ ] Run instrumentation and assemble tests.
- [ ] Commit with `feat: add guided analyze category flow`.

### Task 6: Evidence Model and False-positive Filtering UI

**Files:**
- Create: `app/src/main/java/com/msa/patcher/evidence/Evidence.kt`
- Create: `app/src/main/java/com/msa/patcher/evidence/EvidenceCorrelator.kt`
- Create: `app/src/main/java/com/msa/patcher/evidence/EvidenceFragment.kt`
- Create: `app/src/main/res/layout/fragment_evidence.xml`
- Create: `app/src/test/java/com/msa/patcher/evidence/EvidenceCorrelatorTest.kt`

**Interfaces:**
- Produces: `Evidence(level, subject, source, detail)` with levels `CONFIRMED`, `STRONG`, `MEDIUM`, `WEAK`, `REJECTED`.
- Produces: `EvidenceCorrelator.correlate(raw: List<RawEvidence>): List<Evidence>`.

- [ ] Add failing tests for known collisions: `FRIDAY` ≠ Frida, `keysexposed` ≠ Xposed, `HiddenApiBypass` ≠ LSPosed module, `sweepAngle` ≠ Pangle.
- [ ] Run tests and verify failures.
- [ ] Implement correlation priority exact class/package/native symbol > API/domain correlation > generic substring.
- [ ] Implement Evidence tabs/filters for Confirmed, Suspected, Rejected, Raw.
- [ ] Run tests and ensure collision cases are rejected.
- [ ] Commit with `feat: add correlated evidence dashboard`.

### Task 7: Report Destination and Dual Confidence

**Files:**
- Create: `app/src/main/java/com/msa/patcher/report/ConfidenceCalculator.kt`
- Create: `app/src/main/java/com/msa/patcher/report/ReportFragment.kt`
- Create: `app/src/main/res/layout/fragment_report.xml`
- Create: `app/src/test/java/com/msa/patcher/report/ConfidenceCalculatorTest.kt`

**Interfaces:**
- Produces: `ConfidenceScores(analysisCoverage: Int, behaviourConfidence: Int)` constrained to 0..100.

- [ ] Add failing tests that coverage and behaviour confidence are independent and bounded.
- [ ] Run tests and verify failures.
- [ ] Implement dual confidence calculation from scanned surfaces and correlated evidence strength.
- [ ] Implement report cards for architecture, high-confidence capabilities, medium findings, rejected evidence, and technical inventory.
- [ ] Run tests and assemble.
- [ ] Commit with `feat: add dual-confidence intelligence report`.

### Task 8: Tools Destination and Legacy Isolation

**Files:**
- Create: `app/src/main/java/com/msa/patcher/tools/ToolsFragment.kt`
- Create: `app/src/main/java/com/msa/patcher/tools/LegacyToolsFragment.kt`
- Create: `app/src/main/res/layout/fragment_tools.xml`
- Create: `app/src/main/res/layout/fragment_legacy_tools.xml`
- Create: `app/src/androidTest/java/com/msa/patcher/LegacyIsolationTest.kt`

**Interfaces:**
- Consumes: ScriptRegistry.
- Produces: Legacy tools list reachable only through Tools > Legacy.

- [ ] Add instrumentation test proving Home and Analyze do not display legacy script names.
- [ ] Run test and confirm failure before isolation exists.
- [ ] Implement Tools screen and collapsed Legacy entry.
- [ ] Filter legacy scripts from all primary analysis screens.
- [ ] Run instrumentation tests.
- [ ] Commit with `feat: isolate legacy tools from analysis flow`.

### Task 9: Visual Polish and Responsive Layout

**Files:**
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/values/dimens.xml`
- Create: `app/src/main/res/values-night/colors.xml`
- Modify: all V8 fragment/card layouts.
- Create: `app/src/androidTest/java/com/msa/patcher/ResponsiveSmokeTest.kt`

**Interfaces:**
- Produces: semantic status styling for Confirmed, Informational, Needs Correlation, High Interest, Not Found, Architecture.

- [ ] Add a smoke test that launches Home in portrait and landscape and verifies all primary actions remain visible/reachable.
- [ ] Run it and verify failure where old layouts do not exist.
- [ ] Apply Material card spacing, typography hierarchy, responsive grids, and dark/light themes without hard-coded device-specific sizes.
- [ ] Run portrait/landscape smoke tests.
- [ ] Commit with `style: modernize MSAPatcher V8 responsive UI`.

### Task 10: Build, Sign, Upgrade, and Install Verification

**Files:**
- Create: `scripts/verify-apk.sh`
- Create: `app/src/androidTest/java/com/msa/patcher/LaunchSmokeTest.kt`

**Interfaces:**
- Produces: installable `MSAPatcher_8_0.apk` and verification report containing SHA-256, ZIP integrity, package, versionName, versionCode, signature scheme, and launch result.

- [ ] Add a launch smoke test asserting title `MSAPatcher` and bottom navigation visibility.
- [ ] Run the smoke test on an emulator/device and verify failure before final packaging if navigation is missing.
- [ ] Build release APK and sign with a dedicated development/build certificate; never expose private key material.
- [ ] Run `scripts/verify-apk.sh` to verify ZIP integrity, package/version identity, and signature metadata.
- [ ] Install cleanly and launch; then test an upgrade from the immediately previous V8 build using the same signing identity.
- [ ] Verify built-in script migration refreshes built-ins but preserves a test user script.
- [ ] Commit with `build: verify MSAPatcher V8 release package`.
