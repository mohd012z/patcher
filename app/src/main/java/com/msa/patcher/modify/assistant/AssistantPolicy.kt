package com.msa.patcher.modify.assistant

enum class AssistantDecision { ALLOW, READ_ONLY_EXPLANATION, BLOCK }

object AssistantPolicy {
    private val sensitiveSecrets = Regex("(?i)(private[-_ ]?key|keystore password|api[-_ ]?secret|password\\s*=)")
    private val bypassIntent = Regex("(?i)(bypass|disable|remove|skip|crack|unlock).{0,40}(license|licensing|signature|integrity|paid|premium|protection)")
    private val maliciousHook = Regex("(?i)(stealth|hide|evade).{0,30}(hook|root|detection)")

    fun decide(request: String): AssistantDecision = when {
        bypassIntent.containsMatchIn(request) || maliciousHook.containsMatchIn(request) -> AssistantDecision.READ_ONLY_EXPLANATION
        else -> AssistantDecision.ALLOW
    }

    fun sanitizeContext(context: String): String =
        context.lineSequence().filterNot { sensitiveSecrets.containsMatchIn(it) }.joinToString("\n").take(4000)
}
