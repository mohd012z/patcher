package com.msa.patcher.modify.code

import org.junit.Assert.*
import org.junit.Test

class SmaliQuickCodeTest {
    @Test fun choosesReturnInstructionByDescriptor() {
        assertEquals("return-void", SmaliQuickCode.suggestReturn("()V").snippet?.code)
        assertEquals("return v0", SmaliQuickCode.suggestReturn("()Z").snippet?.code)
        assertEquals("return-object v0", SmaliQuickCode.suggestReturn("()Ljava/lang/String;").snippet?.code)
        assertEquals("return-wide v0", SmaliQuickCode.suggestReturn("()J").snippet?.code)
        assertNull(SmaliQuickCode.suggestReturn("()?").snippet)
    }
}
