package com.cjcrafter.weaponmechanicssupport.command

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.listeners.thread.DescriptiveTitleListener
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class GenerateNameCommand(
    private val openai: OpenAI,
) : CommandWrapper(
    "generatename",
    "Regenerates the thread title using the OpenAI API ",
    Permission.ADMINISTRATOR,
) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val threadChannel = event.channel
        if (threadChannel !is ThreadChannel) {
            event.reply("Lock command must be run in a forum thread").setEphemeral(true).queue()
            return
        }

        val forumChannel = threadChannel.parentChannel
        if (forumChannel !is ForumChannel) {
            event.reply("Thread was not in a forum channel").setEphemeral(true).queue()
            return
        }

        // Ask ChatGPT to generate a better title.
        threadChannel.retrieveStartMessage().queue({ first ->
            val newTitle = DescriptiveTitleListener.updateTitle(openai, threadChannel, first)
            event.reply("New title: $newTitle").setEphemeral(true).queue()
        }, {
            // message was deleted
            event.reply("First message was deleted").setEphemeral(true).queue()
        })
    }
}
