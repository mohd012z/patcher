# MSAPatcher V8.0 — Android Intelligence Lab

This project replaces the legacy "All scripts" launcher with a native Android UI built around five destinations:

- Home — APK selection, precheck, Quick Scan / Deep Scan entry
- Analyze — 12 guided analysis categories
- Evidence — correlated evidence and false-positive filtering
- Report — dual confidence model (Analysis Coverage vs Behaviour Confidence)
- Tools — advanced utilities; legacy scripts are isolated here

## Identity

- Application ID: `com.msa.patcher`
- Version name: `8.0`
- Version code: `80`
- Minimum Android: API 26
- Target / compile SDK: API 35

## Build on GitHub

Push the project to GitHub and run **Build MSAPatcher V8** under Actions. The workflow provisions Java 17 + Gradle 8.9, generates the Gradle wrapper, runs unit tests, builds the debug APK, calculates its SHA-256, and uploads it as the `MSAPatcher-8.0-debug` artifact.

The workflow intentionally generates/refreshes the wrapper because this creation environment does not have a local Gradle installation to generate the official `gradle-wrapper.jar` binary.

## Analysis engine

The V7.4 script bundle is retained under `app/src/main/assets/scripts/`. `index.json` marks analysis categories and keeps legacy utilities out of the primary UI.

The guided V8 UI is designed for static/read-only analysis. It does not add workflows for bypassing third-party licensing, billing, signatures, or protections.
