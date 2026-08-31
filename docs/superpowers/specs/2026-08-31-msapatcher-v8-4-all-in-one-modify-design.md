# MSAPatcher V8.4 All-in-One Modify Workspace Design

Goal: upgrade V8.3 Modify Workspace into a modular, guided editor for APKs the user owns or is authorized to modify.

Sections: Files, Manifest, Search, Converter, Code Tools, Diff, Build, plus a floating assistant.

Files: complete extracted inventory, preview bounded plaintext, replace existing editable assets/resources, preserve original APK.

Manifest: inspect and edit direct plaintext versionName/versionCode/appLabel only. Binary AXML is LIMITED.

Search: filename/path search and bounded content search. Text formats include XML/JSON/TXT/properties/CSV/HTML/JS/CSS. DEX/native can expose bounded printable strings as read-only search evidence.

Help/Auto Suggest: important fields have inline hints, Help/Example and explicit suggestions. Suggestions never auto-apply.

Converter: decimal/hex/binary/octal, UTF-8/text, Base64, hex bytes, URL encoding, byte-size/endian helpers. Language converter uses ML Kit translation with optional source auto-detection and Preserve Format guards for markup/placeholders/resource references/JSON keys.

Code Tools: ordinary Smali syntax explanation and quick snippets for booleans, constants, returns, conditions and registers. No automatic DEX rewrite; no protection/licensing/signature/integrity bypass helpers.

Diff: mutation list, before/after sizes, bounded textual preview, undo.

Build: preflight validation, unsigned rebuild, export, readable warnings. Stale META-INF signature metadata is omitted because it no longer represents modified bytes.

Assistant: draggable/closeable bubble, selected file/search/converter/build context, local conversion/search/syntax help. Provider-ready architecture; no embedded secret/API key.

Version target: 8.4 / code 84 after CI passes.
