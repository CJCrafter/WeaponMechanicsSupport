package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.weaponmechanicssupport.lock.YamlLock
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
            yamlLock.applyLock(thread)
        }

        // Check if .txt files are incomplete logs
        for (attachment in message.attachments) {
            if (attachment.fileExtension != "txt" && attachment.fileExtension != "text")
                continue

            attachment.proxy.download().thenAccept {
                if (IncompleteLogListener.isIncompleteLog(it.bufferedReader().readText())) {
                    yamlLock.applyLock(thread)
                }
            }
        }
    }

    companion object {
        fun hasYaml(content: String): Boolean {
            val regex = """(^\s*[\w\-]+\s*:\s*.*${'$'}\s*(^\s+[\w\-]+\s*:\s*.*${'$'})+)+""".toRegex(RegexOption.MULTILINE)
            val hasYaml = regex.containsMatchIn(content)

            // It is OK to send yaml IF it has a code block
            val hasCodeBlock = content.contains("```yml") || content.contains("```yaml")
            return hasYaml && !hasCodeBlock
        }
    }

}