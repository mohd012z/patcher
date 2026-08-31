# MSAPatcher V8.3 Modify Workspace Design

## Goal
Add a real, offline APK modification workspace to MSAPatcher V8.3 for APKs the user owns or is authorized to modify.

## Scope
The feature operates on a copied workspace. It does not patch the selected APK in place.

Supported mutation paths:
- Replace files in `assets/`.
- Replace files in `res/`, including icons/images when the exact compiled resource path already exists.
- Edit small plaintext files in `assets/` or `res/raw/`.
- Edit `AndroidManifest.xml` only when it is plaintext XML.
- Change plaintext manifest `versionName`, `versionCode`, and direct application label values.
- Undo the latest mutation.
- Show a mutation log/diff.
- Rebuild a new APK archive.
- Export the rebuilt APK through Android's Storage Access Framework.

Explicit limitations:
- Binary Android AXML is detected and reported as limited instead of being rewritten.
- `resources.arsc` is not rewritten.
- DEX and native library mutation are not provided.
- Old signature metadata is not carried into a rebuilt modified archive because it no longer represents the modified content.
- The rebuilt APK is unsigned. The user must sign it with their own authorized signing identity before installation.
- No licensing bypass, paid-feature unlocking, signature/integrity bypass, runtime hooking, or protection circumvention.

## Architecture
The feature is split into:
1. `WorkspacePolicy` — decides which archive paths are editable.
2. `ApkWorkspaceEngine` — safe ZIP extraction, mutation, undo, and rebuild.
3. `ManifestTextEditor` — bounded plaintext-manifest metadata editing.
4. `ModifyFragment` — Android Storage Access Framework UI and status.
5. `ToolsFragment` — entry point into the Modify Workspace.

## Security and Integrity
- ZIP-slip paths are rejected.
- Workspace writes are confined under app cache.
- Text editing is bounded to 512 KiB.
- Archive entries under DEX/native/signing/resource-table internals are read-only.
- Rebuild uses a new output file and never overwrites the original APK.
- Signature artifacts from the source are omitted from the modified rebuild because their digests are stale after mutation.

## UX
Tools shows a `Modify APK Workspace` card.
The Modify screen provides:
- source APK selection or reuse of the APK already selected on Home;
- create workspace;
- editable-entry spinner;
- load/save plaintext entry;
- replace selected resource/asset with a file;
- edit supported plaintext manifest metadata;
- undo last change;
- mutation log;
- rebuild unsigned APK;
- export rebuilt APK.

## Success Criteria
1. A selected APK can be copied and safely extracted.
2. An asset/resource replacement changes the workspace.
3. A plaintext entry can be edited and saved.
4. A plaintext manifest can update supported metadata.
5. Undo restores the last mutated file.
6. Rebuild creates a valid ZIP/APK archive containing changed bytes.
7. Original APK remains unchanged.
8. Unit tests cover path policy, manifest editing, replacement, undo, and rebuild.
