package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.weaponmechanicssupport.lock.YamlLock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class IncompleteYamlListener : ThreadMessageListener {

    private val yamlLock = YamlLock()

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // If the thread is already locked, no need to try to lock it again
        if (thread.appliedTags.contains(yamlLock.getTag(forum)))
            return

        // If the discord message contains log content
        if (hasYaml(message.contentRaw)) {
            val matches = regex.findAll(message.contentRaw)
            val builder = StringBuilder()

            for (match in matches) {
                builder.append("```yaml\n")
                builder.append(match.value)
                builder.append("```\n")
            }

            val embed = EmbedBuilder()
                .setTitle(message.author.name + "'s YAML")
                .setDescription(builder.toString())
                .build()

            message.replyEmbeds(embed).queue();
        }
    }

    companion object {

        private val regex = """^(\s*[\w\-]+\s*:.*\n)+?(\s+[\w\-]+\s*:.*|\s+-\s*.*)*""".toRegex(RegexOption.MULTILINE)

        fun hasYaml(content: String): Boolean {
            val hasYaml = regex.containsMatchIn(content)

            // It is OK to send yaml IF it has a code block
            val hasCodeBlock = content.contains("```yml") || content.contains("```yaml")
            return hasYaml && !hasCodeBlock
        }
    }

}