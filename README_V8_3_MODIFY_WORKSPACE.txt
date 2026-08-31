MSAPatcher V8.3 — Modify Workspace Final Patch

What this adds:
- A real Modify APK Workspace inside Tools.
- Uses selected APK from Home or lets you choose another APK.
- Safe isolated extraction to app cache.
- Edit existing assets/res files.
- Bounded plaintext editor.
- Replace existing asset/resource files.
- Plaintext AndroidManifest metadata editing when supported.
- One-step-at-a-time undo stack.
- Mutation log.
- Rebuild a NEW unsigned APK.
- Export rebuilt APK.
- Original APK is never overwritten.

Important boundaries:
- Binary AndroidManifest.xml is reported as LIMITED for metadata rewrite.
- resources.arsc is not rewritten.
- DEX/native mutation is read-only.
- Rebuilt output is unsigned and must be signed by the user's own authorized signing identity.
- No licensing/signature/integrity bypass functionality.

Apply this ZIP over the ROOT of the existing V8.3 repository.
Version remains 8.3 / versionCode 83.
