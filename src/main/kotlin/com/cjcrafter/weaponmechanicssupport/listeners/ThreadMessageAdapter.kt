package com.cjcrafter.weaponmechanicssupport.listeners

import com.cjcrafter.weaponmechanicssupport.listeners.thread.ThreadMessageListener
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ThreadMessageAdapter(vararg val listeners: ThreadMessageListener) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {

        // Only respond to forum posts
        if (!event.isFromThread) return

        val thread = event.channel.asThreadChannel()
        val forum = thread.parentChannel as? ForumChannel
        if (forum == null || forum.name != "weaponmechanics-support") return

        // Make sure that the thread is specifically asking for help
        if (!forum.availableTags.any { tag -> tag.name == "help" })
            return

        for (listener in listeners) {
            if (!listener.allowBotMessages() && event.author.isBot)
                continue

            listener.onThreadMessage(forum, thread, event.message, event)
        }
    }
}
