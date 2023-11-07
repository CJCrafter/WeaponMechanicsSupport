package com.cjcrafter.weaponmechanicssupport.command

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.weaponmechanicssupport.Main
import com.cjcrafter.weaponmechanicssupport.listeners.thread.GitbookAnswerListener
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class AskCommand: CommandWrapper(
    "ask",
    "Asks the WeaponMechanics wiki a question",
    Permission.MESSAGE_SEND,
) {

    override fun getOptions(): List<OptionData> {
        return listOf(
            OptionData(OptionType.STRING, "plugin", "Which plugin is this for", true)
                .addChoice("weaponmechanics", "weaponmechanics")
                .addChoice("deluxecombat", "deluxecombat"),
            OptionData(OptionType.STRING, "question", "The question to ask", true)
        )
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        val plugin = event.getOption("plugin")?.asString ?: "weaponmechanics"
        val question = event.getOption("question")!!.asString

        val gitbook = Main.gitbooks[plugin] ?: run {
            println("")
            return
        }

        val answer = GitbookAnswerListener.answer(gitbook, question, plugin)
        if (answer == null) {
            event.hook.editOriginal("I'm sorry, but I could not find any good answers for your question:\n$question").queue()
            return
        }

        event.hook.editOriginalEmbeds(answer.first).apply {
            if (answer.second.isNotEmpty())
                this.setActionRow(answer.second)
        }.queue()
    }
}
