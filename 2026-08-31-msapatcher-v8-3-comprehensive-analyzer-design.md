# MSAPatcher V8.3 Comprehensive Analyzer Design

## Goal
Transform MSAPatcher from a lightweight ZIP/string sampler into an offline, read-only APK static analysis application that produces structured evidence across the existing Analyze, Evidence, and Report tabs.

## Scope
V8.3 remains a static analyzer. It must not modify APKs, bypass protections, disable licensing, patch signatures, hook another app, or execute third-party APK code.

## Architecture
The scanner becomes a pipeline:
1. APK archive inventory.
2. Manifest inspection.
3. DEX metadata and string/API indicator analysis.
4. Resources/assets/config inspection.
5. Network endpoint extraction.
6. Native/JNI inventory.
7. Signing/integrity metadata.
8. Evidence aggregation into existing 12 categories.

Each analyzer returns structured evidence with category, title, detail, confidence, source, and evidence type. Analyze maps evidence into category states; Evidence renders the raw structured records; Report summarizes coverage and limitations.

## Data Model
Add structured evidence fields:
- category
- title
- detail
- confidence
- source
- evidenceType

Add analysis coverage fields:
- analyzedEntries
- sampledBytes
- analyzersRun
- analyzersLimited
- warnings

## Manifest Analyzer
Read AndroidManifest.xml from the APK and extract as much safely available metadata as possible without executing the app:
- package name
- version metadata
- min/target SDK when available
- permissions
- activities
- services
- receivers
- providers
- exported components
- intent filters when detectable

If binary AXML cannot be fully decoded with the current dependency set, return LIMITED evidence rather than fabricating results.

## DEX Analyzer
Inspect classes*.dex metadata and printable content to provide:
- DEX count
- estimated class descriptor count
- method/reference indicators
- package namespace indicators
- reflection/dynamic loading indicators
- WebView indicators
- crypto/API indicators
- runtime/hook-related strings as evidence only

No decompilation or code rewriting is included in V8.3.

## Resources Analyzer
Inspect:
- res/
- assets/
- META-INF/
- XML/JSON/properties/text assets
- Flutter/Unity/React Native markers
- embedded config files

Report counts and notable configuration evidence.

## Network Analyzer
Extract static URL/domain/host evidence from sampled static content and resource files. Normalize duplicates and record the source entry. Do not connect to discovered endpoints.

## Native Analyzer
Inspect lib/<abi>/*.so entries and report:
- ABI
- library name
- JNI/native presence
- framework/native markers
- suspicious runtime instrumentation indicators only as static evidence

## Signing & Integrity Analyzer
Read META-INF certificate/signature-related archive entries where available and report:
- signing artifacts present
- certificate/signature file names
- archive digest/integrity metadata already computed by precheck
- limitations of static verification

No signature removal, replacement, or bypass functionality.

## Analyze UX
All 12 existing categories remain visible.
States:
- FOUND: evidence exists
- CLEAN: analyzer ran and found no indicator
- READY: inventory/summary available
- LIMITED: analyzer could not provide full coverage
- ERROR: analyzer failed safely

Tap a category to show:
- status
- evidence count
- source files
- confidence
- explanations
- analyzer limitations

## Evidence UX
Evidence tab becomes the detailed record browser and should show source path per finding.

## Report UX
Report shows:
- scan mode
- APK metadata
- analyzers completed
- analyzers limited
- category result counts
- evidence totals by confidence
- network/native/framework summaries
- explicit static-analysis limitation statement

## Performance
- Work off the UI thread.
- Keep bounded per-entry reads.
- Deep Scan should broaden coverage but still avoid loading the full APK into memory.
- Avoid duplicate reading where possible.
- Continue to support Android minSdk 26 and Java 17.

## Testing
Pure analyzers and mappers must have JUnit tests.
Regression tests must cover:
- category mapping
- URL deduplication
- DEX metadata indicators
- native ABI extraction
- manifest fallback/limited behavior
- analyzer aggregation
- V8.3 version identity

## Success Criteria
V8.3 is successful when:
1. Quick/Deep Scan completes without UI freeze on ordinary APKs.
2. Analyze contains meaningful category-specific results instead of generic read-only summaries.
3. Evidence shows source-aware findings.
4. Report reflects actual analyzer coverage.
5. CI passes unit tests, debug build, APK verification, and artifact upload.
