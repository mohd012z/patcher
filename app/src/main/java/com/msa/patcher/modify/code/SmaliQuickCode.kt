package com.msa.patcher.modify.code

object SmaliQuickCode {
    val catalog: List<SmaliSnippet> = listOf(
        SmaliSnippet("bool_true", "Boolean true", "const/4 v0, 0x1", "Loads integer 1, commonly used as boolean true.", "v0", "Boolean"),
        SmaliSnippet("bool_false", "Boolean false", "const/4 v0, 0x0", "Loads integer 0, commonly used as boolean false.", "v0", "Boolean"),
        SmaliSnippet("return_void", "Return void", "return-void", "Returns from a method whose return type is V.", "none", "Return"),
        SmaliSnippet("return_value", "Return value", "return v0", "Returns a single-width primitive value from v0.", "v0", "Return"),
        SmaliSnippet("return_object", "Return object", "return-object v0", "Returns an object/reference stored in v0.", "v0", "Return"),
        SmaliSnippet("return_wide", "Return wide", "return-wide v0", "Returns a wide J/D value using v0/v1.", "v0,v1", "Return"),
        SmaliSnippet("if_eqz", "If zero", "if-eqz v0, :label", "Branches when v0 equals zero/null.", "v0", "Condition"),
        SmaliSnippet("if_nez", "If non-zero", "if-nez v0, :label", "Branches when v0 is non-zero/non-null.", "v0", "Condition"),
        SmaliSnippet("const_small", "Small constant", "const/4 v0, 0x0", "Loads a small integer constant into v0.", "v0", "Constant")
    )

    fun suggestReturn(descriptor: String): SmaliSuggestion {
        val type = descriptor.trim().substringAfterLast(')').ifBlank { descriptor.trim() }
        val snippetId = when {
            type == "V" -> "return_void"
            type == "J" || type == "D" -> "return_wide"
            type.startsWith("L") || type.startsWith("[") -> "return_object"
            type in setOf("Z", "B", "S", "C", "I", "F") -> "return_value"
            else -> null
        }
        val snippet = catalog.firstOrNull { it.id == snippetId }
        return if (snippet != null) SmaliSuggestion(snippet, "Compatible with return descriptor $type")
        else SmaliSuggestion(null, "Unknown or unsupported return descriptor: $type")
    }

    fun explain(line: String): String {
        val trimmed = line.trim()
        return catalog.firstOrNull { trimmed.startsWith(it.code.substringBefore(' ')) }?.explanation
            ?: when {
                trimmed.startsWith(".method") -> "Declares a Smali method. The descriptor after the method name defines parameter and return types."
                trimmed.startsWith(".locals") -> "Declares how many local registers the method uses."
                trimmed.startsWith(".registers") -> "Declares the total register count for the method."
                trimmed.startsWith("invoke-") -> "Invokes another method; inspect the method descriptor and argument registers."
                else -> "No built-in explanation for this line. Use it as read-only context for the assistant."
            }
    }
}
