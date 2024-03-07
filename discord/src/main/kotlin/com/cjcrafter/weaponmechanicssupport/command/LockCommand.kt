package com.cjcrafter.weaponmechanicssupport.command

import com.cjcrafter.weaponmechanicssupport.listeners.thread.ThreadMessageListener
import com.cjcrafter.weaponmechanicssupport.lock.LatestLogLock
import com.cjcrafter.weaponmechanicssupport.lock.Lock
import com.cjcrafter.weaponmechanicssupport.lock.YamlLock
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.concurrent.TimeUnit

class LockCommand : CommandWrapper(
    "lock",
    "Applies a tag to the forum thread",
    Permission.ADMINISTRATOR
) {

    private val locks = listOf(
        LatestLogLock(),
        YamlLock(),
    )

    override fun getOptions(): List<OptionData> {
        return listOf(OptionData(OptionType.STRING, "lock", "which lock to use", true).apply {
            for (lock in locks) {
                addChoice(lock.name, lock.forumTag)
            }
        })
    }

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

        val lockName = event.getOption("lock")?.asString ?: run { event.reply("Missing lock argument").setEphemeral(true).queue(); return }
        val lock = locks.find { it.name == lockName }
        val tag = forumChannel.availableTags.find { it.name == lock?.forumTag }
        if (lock == null || tag == null) {
            event.reply("Tag $lockName (forum tag ${lock?.forumTag}) does not exist in the forum $forumChannel").setEphemeral(true).queue()
            return
        }

        // Add the lock tag to the channel
        lock.applyLock(threadChannel)
        event.reply("Locked thread").setEphemeral(true).queue()
    }
}