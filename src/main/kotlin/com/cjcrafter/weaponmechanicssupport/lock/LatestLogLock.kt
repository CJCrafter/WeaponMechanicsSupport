package com.cjcrafter.weaponmechanicssupport.lock

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Forces the user to send their latest.log file before they can continue their thread.
 */
class LatestLogLock : Lock("log") {

    override fun sendLockAlert(thread: ThreadChannel) {
        thread.guild.retrieveMemberById(thread.ownerId).queue({ member ->
            val embed = EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(
                    """
                        Hello ${member.asMention}!
                        
                        Sorry that you are having troubles! For us to assist you further, we need your `latest.log` file. Here's how to find it:
                        
                        > 1. Go to your server folder.
                        > 2. Open the `logs` directory.
                        > 3. Look for the file named `latest.log`.
                        
                        Please attach the `latest.log` file in your next message. Until then, this thread will be on hold. We appreciate your understanding!
                    """.trimIndent()
                )
                .setImage("https://user-images.githubusercontent.com/43940682/218565746-d3692524-d652-4276-8e96-8be480d77bfd.gif")
                .setTimestamp(Instant.now())
                .build()

            thread.sendMessageEmbeds(embed).queue()
        }, { /* ignore, member left server */})
    }

    override fun shouldUnlock(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ): Boolean {

        // The message should contain an attachment file named "latest.log,"
        // but any .log file is acceptable (Since they may send an older log).
        for (attachment in message.attachments) {
            if (attachment.fileExtension == "log")
                return true
        }

        return false
    }

    override fun stillLockedAlert(message: Message) {
        message.reply("I'm sorry, but you did not send a log file. Please refer to the message above.").queue { alert ->
            alert.delete().queueAfter(1, TimeUnit.MINUTES)
        }
    }

    override fun removeLock(thread: ThreadChannel) {
        val forum = thread.parentChannel as ForumChannel
        val logLockTag = getTag(forum)
        if (logLockTag == null) {
            println("Could not find '$forumTag' tag in $forum")
            return
        }

        // if the user attaches a log, we can remove the tag
        val tags = LinkedHashSet(thread.appliedTags)
        tags.remove(logLockTag)
        thread.manager.setAppliedTags(tags).queue()
        println("Removed the $name lock tag from $thread")

        thread.history.retrievePast(20).queue { messages ->
            val lockEmbed = messages.find { message ->
                message.embeds.any { embed ->
                    embed.description?.contains("we need your `latest.log` file.") ?: false
                }
            }

            lockEmbed?.delete()?.queue()
        }
    }
}
