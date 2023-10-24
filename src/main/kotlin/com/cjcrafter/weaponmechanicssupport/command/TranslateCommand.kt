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
        var messageId = event.getOption("messageId")?.asString
        if (messageId == null) {
            messageId = event.channel.latestMessageId
        }

        val message = event.channel.retrieveMessageById(messageId).complete()
        if (message == null) {
            event.reply("Message $messageId not found").setEphemeral(true).queue()
            return
        }

        val translation = TranslateMessageListener.translateMessage(openai, message.contentRaw)
        if (translation == null) {
            event.reply("\"${message.contentRaw}\" was already english!").setEphemeral(true).queue()
            return
        }

        message.reply("""
            ### Translation
            *$translation*
            """.trimIndent()).setAllowedMentions(emptyList()).queue()
    }
}
