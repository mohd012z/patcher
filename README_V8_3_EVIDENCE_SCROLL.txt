MSAPatcher V8.3 — Evidence Scroll Final Patch

Scope:
- Keeps version 8.3.
- Adds a persistent Evidence scroll position indicator.
- Shows "Scroll 0–100% • Evidence x/y".
- Keeps the vertical scrollbar visible instead of fading away.
- Keeps the Evidence heading/status outside the scrolling content.
- Does not change scanner/analyzer behavior.

Copy the contents of this patch over the ROOT of the MSAPatcher project.

Files added:
- app/src/main/java/com/msa/patcher/evidence/EvidenceScrollState.kt
- app/src/test/java/com/msa/patcher/evidence/EvidenceScrollStateTest.kt

Files replaced:
- app/src/main/java/com/msa/patcher/evidence/EvidenceFragment.kt
- app/src/main/res/layout/fragment_evidence.xml
