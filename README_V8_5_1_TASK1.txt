MSAPatcher V8.5.1 — Task 1 Adaptive Display + UI Settings

Apply this patch at the PROJECT ROOT.

Adds:
- DisplayProfile: phone/landscape/tablet classification from dp + density.
- Compact-first WorkspaceUiSettings.
- UI scale/font/editor font/button spacing/toolbar/tab/AI bubble/view/orientation/word-wrap/line-number/zoom-memory settings.
- SharedPreferences adapter with a testable backend abstraction.
- Unit tests for adaptive profile and settings normalization/persistence.

This task intentionally does not modify ModifyFragment or fragment_modify.xml yet.
That is Task 2, after Task 1 review.

Expected test commands in the full Android project:
  ./gradlew testDebugUnitTest --tests "*DisplayProfileTest"
  ./gradlew testDebugUnitTest --tests "*WorkspaceUiSettingsStoreTest"

Safety:
- No APK mutation logic changed.
- No DEX/native mutation.
- No secrets or keys.
