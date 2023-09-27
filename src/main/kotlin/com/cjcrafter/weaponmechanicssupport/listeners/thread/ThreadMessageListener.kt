package com.cjcrafter.weaponmechanicssupport.listeners.thread

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface ThreadMessageListener {
    fun allowBotMessages() = false

    fun onThreadMessage(forum: ForumChannel, thread: ThreadChannel, message: Message, event: MessageReceivedEvent)
}