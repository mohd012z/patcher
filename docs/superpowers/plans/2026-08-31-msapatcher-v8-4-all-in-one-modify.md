# MSAPatcher V8.4 All-in-One Modify Workspace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade V8.3 into a modular V8.4 workspace with Files, Manifest, Search, Converter, Code Tools, Diff, Build, guided inputs, auto-suggest, and an optional contextual AI assistant.

**Architecture:** Keep `ApkWorkspaceEngine` as the safe mutation/rebuild boundary. Refactor the current large `ModifyFragment` into a workspace coordinator backed by focused pure-Kotlin services for search, conversion, suggestions, diff, build validation and Smali syntax assistance.

**Tech Stack:** Kotlin 2.0.21, AndroidX Fragment/Lifecycle, Material UI, Java 17, Gradle 8.9, AGP 8.7.3, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-08-31-msapatcher-v8-4-all-in-one-modify-design.md`

## Global Constraints
- package `com.msa.patcher`; minSdk 26; target/compileSdk 35; Java 17.
- Release only after CI passes: `versionName 8.4`, `versionCode 84`.
- Original APK is never overwritten; path confinement and ZIP-slip protection remain mandatory.
- Binary AXML/resources/DEX/native mutation remains LIMITED/read-only unless a separately verified rebuild layer exists.
- Core workspace/search/converter/diff/rebuild remain usable without AI/network.
- Suggestions/AI never auto-apply mutations.
- No licensing bypass, paid-feature unlocking, signature/integrity bypass, protection circumvention, malicious hooking, or private-key logging.

---

### Task 1 — Workspace shell
**Files:** create `WorkspaceSection.kt`; modify `ModifyFragment.kt`, `fragment_modify.xml`; test `WorkspaceSectionTest.kt`.
- [ ] RED test seven ordered sections: Files, Manifest, Search, Converter, Code Tools, Diff, Build.
- [ ] Implement enum/title model and section switching while preserving choose/create/export flow.
- [ ] Run focused + full unit tests GREEN.
- [ ] Commit `Refactor V8.4 modify workspace shell`.

### Task 2 — File browser and path search
**Files:** create `modify/search/SearchModels.kt`, `WorkspaceSearch.kt`; modify engine/UI; test `WorkspaceSearchTest.kt`.
- [ ] RED tests: case-insensitive path search, filters, protected/read-only labeling, empty query.
- [ ] Implement bounded complete workspace inventory and filters All/Manifest/Resources/Assets/DEX/Native/Config.
- [ ] Results expose only actions allowed by `WorkspacePolicy`.
- [ ] Tests GREEN; commit `Add V8.4 workspace file search`.

### Task 3 — Content search
**Files:** extend `WorkspaceSearch.kt`, engine and tests.
- [ ] RED tests XML/JSON/TXT matches, binary rejection, size bound, DEX/native printable-string read-only hits.
- [ ] Implement bounded content search with path, match/context and count.
- [ ] DEX/native results remain preview/search only.
- [ ] Tests GREEN; commit `Add V8.4 content search`.

### Task 4 — Help and Auto Suggest
**Files:** create `modify/help/FieldHelp.kt`, `WorkspaceSuggestions.kt`; test `WorkspaceSuggestionsTest.kt`; modify UI.
- [ ] RED tests for Search, App Label, Version Name/Code and Output Name suggestions.
- [ ] Implement help registry with title, description, example and inert suggestions.
- [ ] Add inline hints, `?` Help/Example and suggestion chips; selection fills a field but never saves/applies it.
- [ ] Tests GREEN; commit `Add V8.4 guided input suggestions`.

### Task 5 — Data Converter
**Files:** create `modify/converter/DataConverter.kt`, `ConversionModels.kt`; test `DataConverterTest.kt`; modify UI.
- [ ] RED tests: 255→FF/11111111/377; Hello↔SGVsbG8=; UTF-8 hex; URL round-trip; endian; ambiguous detection.
- [ ] Implement decimal/hex/binary/octal, text/UTF-8/Base64/hex bytes, ASCII/Unicode, URL, byte-size and endian helpers with bounded input.
- [ ] Add Convert/Copy/Insert/Search/Send-to-Assistant actions.
- [ ] Tests GREEN; commit `Add V8.4 data converter`.

### Task 6 — Language Converter
**Files:** create `TranslationGuard.kt`, `LanguageConverterModel.kt`; test `TranslationGuardTest.kt`; modify UI.
- [ ] RED tests preserving `%s`, `%1$d`, escaped newlines, XML tags and Android resource references through a mock translation round-trip.
- [ ] Implement source/target, Auto Detect, Swap and Preserve Format state without a hard network dependency.
- [ ] Translation output is separate until Preview/Diff + explicit Apply.
- [ ] Tests GREEN; commit `Add V8.4 language converter guards`.

### Task 7 — Smali Quick Code
**Files:** create `modify/code/SmaliModels.kt`, `SmaliQuickCode.kt`; test `SmaliQuickCodeTest.kt`; modify UI.
- [ ] RED tests for void, boolean/int, object and wide return-type instruction selection plus unknown signature rejection.
- [ ] Implement ordinary syntax catalog: boolean/constants, returns, conditions, registers and method-return helper.
- [ ] Show explanation, snippet preview and affected registers before Insert; never auto-apply/rebuild.
- [ ] Exclude licensing/signature/integrity/protection-bypass templates.
- [ ] Tests GREEN; commit `Add V8.4 Smali quick helpers`.

### Task 8 — Diff and undo
**Files:** create `modify/diff/WorkspaceDiff.kt`; test `WorkspaceDiffTest.kt`; modify engine/UI.
- [ ] RED tests modified/replaced/restored entries, before/after sizes and bounded text preview.
- [ ] Implement mutation-derived diff without unbounded binary loads.
- [ ] Add Diff panel and supported per-entry/latest undo.
- [ ] Tests GREEN; commit `Add V8.4 workspace diff`.

### Task 9 — Build preflight
**Files:** create `modify/build/BuildPreflight.kt`; test `BuildPreflightTest.kt`; modify engine/UI.
- [ ] RED tests missing workspace, invalid state, no-change warning, stale-signature notice and valid rebuild-ready state.
- [ ] Implement structured validation/progress/log state around existing unsigned rebuild.
- [ ] Block rebuild on validation errors; export remains explicit and never asks for pasted private keys.
- [ ] Tests GREEN; commit `Add V8.4 build preflight`.

### Task 10 — Contextual AI Assistant
**Files:** create `modify/assistant/AssistantContext.kt`, `AssistantPolicy.kt`; test `AssistantPolicyTest.kt`; modify UI/layout.
- [ ] RED tests bounded context, credential/private-key exclusion, allowed Explain/Convert/Search/syntax help and blocked bypass-oriented mutation requests.
- [ ] Implement provider interface plus offline fallback for help/converter explanations.
- [ ] Add draggable closeable bubble, persistent reopen control, message panel and quick actions Explain/Convert/Search/Open/Insert Suggestion.
- [ ] Verify X truly closes panel and reopen restores it.
- [ ] Tests GREEN; commit `Add V8.4 contextual assistant shell`.

### Task 11 — Manifest integration
**Files:** modify `ManifestTextEditor.kt`, tests and UI.
- [ ] RED tests direct plaintext version/label editing and rejection of resource-reference label/binary manifest mutation.
- [ ] Display editable vs LIMITED fields clearly with Help/Auto Suggest and preview-before-apply.
- [ ] Tests GREEN; commit `Integrate V8.4 manifest workspace panel`.

### Task 12 — Release integration
**Files:** modify `app/build.gradle.kts`, `.github/workflows/android-build.yml`; create `README_V8_4_MODIFY_WORKSPACE.txt`.
- [ ] Run `./gradlew testDebugUnitTest --stacktrace` → PASS.
- [ ] Run `./gradlew assembleDebug --stacktrace` → PASS.
- [ ] Run `unzip -t app/build/outputs/apk/debug/app-debug.apk` → no ZIP errors.
- [ ] Set `versionCode 84`, `versionName "8.4"`; artifact `MSAPatcher-8.4-debug`.
- [ ] Document supported/LIMITED operations and authorized-use boundary.
- [ ] Re-run tests/build after version changes.
- [ ] Commit `Release MSAPatcher V8.4 modify workspace` and push.
- [ ] Inspect matching GitHub Actions run; call V8.4 complete only when Unit tests, Build debug APK, Verify APK and Upload APK all succeed.
