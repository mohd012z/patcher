package com.msa.patcher.modify

import org.junit.Assert.*
import org.junit.Test

class WorkspacePolicyTest {
    @Test fun editablePathsAreBounded() {
        assertTrue(WorkspacePolicy.isEditable("assets/config.json"))
        assertTrue(WorkspacePolicy.isEditable("res/drawable/logo.png"))
        assertTrue(WorkspacePolicy.isEditable("AndroidManifest.xml"))
        assertFalse(WorkspacePolicy.isEditable("classes.dex"))
        assertFalse(WorkspacePolicy.isEditable("lib/arm64-v8a/libx.so"))
        assertFalse(WorkspacePolicy.isEditable("resources.arsc"))
        assertFalse(WorkspacePolicy.isEditable("META-INF/CERT.RSA"))
        assertNull(WorkspacePolicy.normalizeArchivePath("../outside"))
    }

    @Test fun identifiesSignatureArtifacts() {
        assertTrue(WorkspacePolicy.isSignatureArtifact("META-INF/CERT.SF"))
        assertTrue(WorkspacePolicy.isSignatureArtifact("META-INF/CERT.RSA"))
        assertFalse(WorkspacePolicy.isSignatureArtifact("assets/CERT.RSA"))
    }
}
