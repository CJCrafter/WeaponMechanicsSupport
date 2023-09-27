package com.cjcrafter.weaponmechanicssupport.lock

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit

class YamlLock : Lock("yaml") {

    override fun sendLockAlert(thread: ThreadChannel) {
        thread.guild.retrieveMemberById(thread.ownerId).queue({ member ->
            val embed = EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(
                    """
                        Hello ${member.asMention}!
                        
                        Sorry that you are having troubles! For us to assist you further, we need you to format your YAML properly, or to upload it as a `.yml` file. Here's how to do that:
                        
                        ## Formatting
                        > 1. Surround your YAML with three backticks (```)
                        > 2. Add `yaml` after the first set of backticks
                        > 3. (See image below for example)
                        
                        ## Uploading your File
                        > 1. Find your `.yml` file.
                        > 2. Drag and drop the file into your message in Discord.
                        
                        Please attach the `.yml` file in your next message. Until then, this thread will be on hold. We appreciate your understanding!
                    """.trimIndent()
                )
                .setImage("https://user-images.githubusercontent.com/43940682/218564371-7acb1e05-b5d3-4ce2-87b0-73a98b64e243.png")
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
        if (message.contentRaw.contains("```yaml") || message.contentRaw.contains("```yml"))
            return true

        for (attachment in message.attachments) {
            if (attachment.fileExtension == "yml" || attachment.fileExtension == "yaml")
                return true
        }

        return false;
    }

    override fun stillLockedAlert(message: Message) {
        message.reply("I'm sorry, but you still need to fix the YAML format or upload the `.yml` file.").queue { alert ->
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
                    embed.description?.contains("we need you to format your YAML properly, or to upload it as a `.yml`") ?: false
                }
            }

            lockEmbed?.delete()?.queue()
        }
    }
}
