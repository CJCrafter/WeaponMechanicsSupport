package com.cjcrafter.weaponmechanicssupport.listeners

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.weaponmechanicssupport.listeners.thread.GitbookAnswerListener
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonClickHandler(
    private val gitbook: GitBookApi
) : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        event.deferReply().queue()

        if (!event.componentId.startsWith("gitbook_"))
            return

        val previousQueries = event.componentId.substringAfterLast("_").split("|")
        val buttonText = event.button.label

        val answer = GitbookAnswerListener.answer(gitbook, buttonText, previousQueries)
        if (answer == null) {
            event.hook.editOriginal("I'm sorry, but I could not find any good answers for that question.").queue()
            return
        }

        event.hook.editOriginalEmbeds(answer.first).apply {
            if (answer.second.isNotEmpty())
                setActionRow(answer.second)
        }.queue {
            event.editButton(event.button.asDisabled()).queue()
        }
    }
}