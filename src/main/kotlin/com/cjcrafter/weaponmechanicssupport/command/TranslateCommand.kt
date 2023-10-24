package com.cjcrafter.weaponmechanicssupport.command

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.listeners.thread.TranslateMessageListener
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class TranslateCommand (
    private val openai: OpenAI,
) : CommandWrapper(
    "translate",
    "Translates the message into English",
    Permission.ADMINISTRATOR,
) {

    override fun getOptions(): List<OptionData> {
        return listOf(OptionData(OptionType.STRING, "messageid", "The message to translate", false))
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        var messageId = event.getOption("messageId")?.asString
        if (messageId == null) {
            messageId = event.channel.latestMessageId
        }

        val message = event.channel.retrieveMessageById(messageId).complete()
        if (message == null) {
            event.hook.editOriginal("Message $messageId not found").queue()
            return
        }

        val translation = TranslateMessageListener.translateMessage(openai, message.contentRaw)
        if (translation == null) {
            event.hook.editOriginal("\"${message.contentRaw}\" was already english!").queue()
            return
        }

        event.hook.editOriginal("""
            ### Translation
            *$translation*
            """.trimIndent()).setAllowedMentions(emptyList()).queue()
    }
}
