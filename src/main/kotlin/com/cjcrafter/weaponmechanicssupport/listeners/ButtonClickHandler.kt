package com.cjcrafter.weaponmechanicssupport.listeners

import com.cjcrafter.weaponmechanicssupport.Main
import com.cjcrafter.weaponmechanicssupport.listeners.thread.GitbookAnswerListener
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonClickHandler() : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        event.deferReply().queue()

        if (!event.componentId.startsWith("gitbook_"))
            return

        val plugin = event.componentId.substringAfterLast("_")
        val buttonText = event.button.label

        val gitbook = Main.gitbooks[plugin] ?: run {
            println("Could not find any gitbook for '$plugin', options are: ${Main.gitbooks.keys}")
            return
        }

        val answer = GitbookAnswerListener.answer(gitbook, buttonText, plugin)
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