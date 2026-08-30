# MSAPatcher V8 Native UI Design

## Goal
Rebuild the MSAPatcher front-end as a modern guided static-analysis application while retaining the existing read-only analysis scripts as the engine layer.

## Product Identity
- App name: MSAPatcher
- Version name: 8.0
- Version code: 80
- Primary subtitle: Android Intelligence Lab
- The V8 launcher must visibly identify itself as MSAPatcher V8.0.

## Navigation
Five primary destinations:
1. Home
2. Analyze
3. Evidence
4. Report
5. Tools

## Home
- APK selector card
- Selected APK summary: filename, size, SHA-256, DEX count, native count, ABI summary
- Quick Scan and Deep Scan buttons
- Analysis Coverage and Behaviour Confidence meters
- Capability cards: DEX, Native, SDK, Network, Root, Hook, Signing, Memory, Resources, Protection
- Recent scan area

## Analyze
Guided analysis flow:
PRECHECK → ARCHITECTURE → CORE → SPECIALIZED → CORRELATE → CONFIDENCE → REPORT

Analysis categories:
1. Overview
2. APK Structure
3. DEX & Code
4. Native & JNI
5. Resources & Build
6. SDK & Network
7. Root & Virtualization
8. Hook & Runtime
9. Signing & Integrity
10. Memory & Process
11. Protection
12. Evidence & Report

Quick Scan runs only the core read-only scanners. Deep Scan selects specialized scanners based on detected architecture.

## Evidence
Evidence is grouped into Confirmed, Strong, Medium, Weak, and Rejected/False Positive. Raw hit counts are never presented as confirmation without correlation. Exact class/package/native-symbol evidence outranks generic substring matches.

## Report
The report shows Architecture, Analysis Coverage, Behaviour Confidence, high-confidence capabilities, medium-confidence findings, rejected false positives, and technical inventory. Report export remains read-only.

## Tools
Legacy utilities are moved under Tools > Legacy and are collapsed by default. Analysis remains the primary experience. No V8 UI should expose third-party billing/license bypass instructions or operational root/hooking guidance.

## Script Engine Compatibility
Existing read-only analysis scripts remain available behind a script registry. The UI must not depend on the legacy alphabetical ListView as the primary navigation. Script metadata is normalized into category, title, description, and mode.

## Built-in Script Migration
The existing app copies built-in scripts to application storage and may keep stale copies across APK updates. V8 must store a built-in script bundle version. At startup, when bundleVersion changes, only built-in scripts are refreshed. User-created scripts are preserved.

## Architecture Families
- Standard Android
- APK Engineering Tool
- Native/JNI Heavy
- Native Hook/ART Tooling
- Root Framework Tool
- Memory/Process Tooling
- Flutter/Dart AOT
- Extreme Multidex
- Hybrid

## Safety Boundary
V8 is an analysis-first build. It may detect root, hooking, signing, billing, licensing, memory, and protection surfaces, but the guided UI must remain focused on static evidence, inventory, defensive classification, and confidence. It must not add workflows that facilitate bypassing third-party licensing, billing, signatures, or protections.

## Success Criteria
- Launch screen is visually distinct from the legacy `All scripts` ListView.
- Home, Analyze, Evidence, Report, and Tools are reachable through native navigation.
- App visibly reports version 8.0 / code 80.
- Updating from V8.x refreshes built-in scripts when their bundle version changes without deleting user scripts.
- Quick Scan produces a summary without requiring the user to choose individual script numbers.
- Deep Scan displays its chosen analysis categories before execution.
- Legacy scripts remain accessible only from Tools > Legacy.
- APK can be built, installed, and opened without crashing.
