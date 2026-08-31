MSAPatcher V8.1 functional scan patch

Fixes:
- APK precheck moved off the UI thread.
- Quick Scan and Deep Scan buttons now have functional handlers.
- Selected APK URI and scan result are shared through HomeViewModel.
- Quick/Deep scans perform read-only static inspection of APK ZIP entries and sampled strings.
- Analyze, Evidence and Report tabs display actual scan output.
- False-positive guard keeps FRIDAY from becoming Frida and keysexposed/navexposed from becoming Xposed.
- Version bumped to 8.1 / versionCode 81.

Apply by extracting this ZIP over the repository root, replacing matching files.
Then run: git add . && git commit -m "Fix V8.1 functional APK scan" && git push origin main
