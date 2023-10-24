package com.cjcrafter.weaponmechanicssupport.command

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.weaponmechanicssupport.listeners.thread.GitbookAnswerListener
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class AskCommand (
    private val gitbook: GitBookApi,
) : CommandWrapper(
    "ask",
    "Asks the WeaponMechanics wiki a question",
    Permission.MESSAGE_SEND,
) {

    override fun getOptions(): List<OptionData> {
        return listOf(OptionData(OptionType.STRING, "question", "The question to ask", true))
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val question = event.getOption("question")!!.asString

        val answer = GitbookAnswerListener.answer(gitbook, question)
        if (answer == null) {
            event.reply("I'm sorry, but I could not find any good answers for that question.").setEphemeral(true).queue()
            return
        }

        event.replyEmbeds(answer.first).apply {
            if (answer.second.isNotEmpty())
                this.setActionRow(answer.second)
        }.queue()
    }
}
