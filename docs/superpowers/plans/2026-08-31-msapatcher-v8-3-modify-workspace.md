# MSAPatcher V8.3 Modify Workspace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a functional offline APK workspace that can mutate authorized APK resources/assets, edit bounded plaintext metadata, undo changes, rebuild a new unsigned APK, and export it.

**Architecture:** Pure-Kotlin workspace logic owns ZIP extraction/mutation/rebuild while Android UI only handles document selection and display. Rebuild never overwrites the source APK and rejects unsafe paths.

**Tech Stack:** Kotlin 2.0.21, AndroidX Fragment/Lifecycle, Material Components, Java 17, Android API 26+, java.util.zip.

**Spec:** `docs/superpowers/specs/2026-08-31-msapatcher-v8-3-modify-workspace-design.md`

## Global Constraints
- Keep application version 8.3 / versionCode 83.
- minSdk 26; targetSdk/compileSdk 35; Java 17.
- Offline operation only.
- No DEX/native mutation, licensing bypass, signature/integrity bypass, or runtime hooking.
- Never overwrite the original APK.
- Rebuilt APK is unsigned.
- Reject ZIP-slip paths and workspace path escapes.

---

### Task 1: Workspace policy and models
**Files:**
- Create: `app/src/main/java/com/msa/patcher/modify/WorkspacePolicy.kt`
- Create: `app/src/main/java/com/msa/patcher/modify/WorkspaceModels.kt`
- Test: `app/src/test/java/com/msa/patcher/modify/WorkspacePolicyTest.kt`

- [ ] Write failing tests for safe editable paths and protected internals.
- [ ] Implement path normalization and editability rules.
- [ ] Verify tests pass.

### Task 2: Plaintext manifest editor
**Files:**
- Create: `app/src/main/java/com/msa/patcher/modify/ManifestTextEditor.kt`
- Test: `app/src/test/java/com/msa/patcher/modify/ManifestTextEditorTest.kt`

- [ ] Write failing tests for `versionName`, `versionCode`, direct label changes, and binary rejection.
- [ ] Implement bounded regex-based plaintext XML mutation.
- [ ] Verify tests pass.

### Task 3: ZIP workspace engine
**Files:**
- Create: `app/src/main/java/com/msa/patcher/modify/ApkWorkspaceEngine.kt`
- Test: `app/src/test/java/com/msa/patcher/modify/ApkWorkspaceEngineTest.kt`

- [ ] Write synthetic-ZIP tests for extract, replacement, undo, and rebuild.
- [ ] Implement safe extraction with method metadata.
- [ ] Implement mutation log and one-step undo stack.
- [ ] Rebuild while omitting stale signature metadata.
- [ ] Verify the rebuilt file is a readable ZIP and contains changed bytes.

### Task 4: Android Modify UI
**Files:**
- Create: `app/src/main/java/com/msa/patcher/modify/ModifyFragment.kt`
- Create: `app/src/main/res/layout/fragment_modify.xml`
- Modify: `app/src/main/java/com/msa/patcher/MainActivity.kt`
- Modify: `app/src/main/java/com/msa/patcher/tools/ToolsFragment.kt`
- Modify: `app/src/main/res/layout/fragment_tools.xml`

- [ ] Add Tools entry point.
- [ ] Reuse Home selected APK URI when available.
- [ ] Add workspace, entry spinner, text editor, replace, manifest edit, undo, rebuild, and export actions.
- [ ] Keep all IO off the UI thread.

### Task 5: Regression verification
- [ ] Run pure-Kotlin tests/harness.
- [ ] Check all patch files and ZIP integrity.
- [ ] User applies patch to project root and pushes.
- [ ] GitHub Actions must pass unit tests, assembleDebug, APK verification, and artifact upload.
