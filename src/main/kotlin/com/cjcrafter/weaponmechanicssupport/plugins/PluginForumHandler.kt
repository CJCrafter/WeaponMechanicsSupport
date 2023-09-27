package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.weaponmechanicssupport.listeners.thread.ThreadMessageListener
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

open class PluginForumHandler(
    val channel: String,
    val listeners: List<ThreadMessageListener> = listOf(),
    val tagFilter: Regex = """help""".toRegex(),
): ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // Only respond to forum posts
        if (!event.isFromThread) return

        val thread = event.channel.asThreadChannel()
        val forum = thread.parentChannel as? ForumChannel
        if (forum?.name != channel) return

        // Make sure that the thread is specifically asking for help
        if (!forum.availableTags.any { tag -> tagFilter.containsMatchIn(tag.name) })
            return

        for (listener in listeners) {
            if (!listener.allowBotMessages() && event.author.isBot)
                continue

            listener.onThreadMessage(forum, thread, event.message, event)
        }
    }
}