package com.msa.patcher.modify.ui

class CommandHubController {
    fun visibleActions(): List<CommandHubAction> = CommandHubAction.entries

    fun titleFor(action: CommandHubAction): String =
        if (action.availableNow) action.title else "${action.title} • staged"
}
