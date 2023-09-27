package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.weaponmechanicssupport.lock.LatestLogLock
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class IncompleteLogListener : ThreadMessageListener {

    private val logLock = LatestLogLock()

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // If the thread is already locked, no need to try to lock it again
        if (thread.appliedTags.contains(logLock.getTag(forum)))
            return

        // If the discord message contains log content
        if (isIncompleteLog(message.contentRaw)) {
            logLock.applyLock(thread)
        }

        // Check if .txt files are incomplete logs
        for (attachment in message.attachments) {
            if (attachment.fileExtension != "txt" && attachment.fileExtension != "text")
                continue

            attachment.proxy.download().thenAccept {
                if (isIncompleteLog(it.bufferedReader().readText())) {
                    logLock.applyLock(thread)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun isIncompleteLog(contentRaw: String): Boolean {
            // Check for a timestamp format, e.g., [HH:MM:SS] and [HH:MM:SS ERROR]
            val timestampRegex = Regex("""\[\d{2}:\d{2}:\d{2}.*?\]""")

            // Check for a typical Java stack trace pattern
            val stackTraceStartRegex = Regex("""(\w+\.){3,}""")

            return timestampRegex.containsMatchIn(contentRaw) || stackTraceStartRegex.containsMatchIn(contentRaw)
        }

    }
}
