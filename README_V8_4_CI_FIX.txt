MSAPatcher V8.4 CI Fix
Root cause: repository .gitignore contains **/build/, so Kotlin package folder modify/build/ was ignored by git.
Fix: move BuildPreflight and its test to modify/preflight/ and update ModifyFragment references.
