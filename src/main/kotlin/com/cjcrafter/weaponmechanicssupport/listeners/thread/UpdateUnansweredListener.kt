package com.cjcrafter.weaponmechanicssupport.listeners.thread

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class UpdateUnansweredListener : ThreadMessageListener {

    override fun allowBotMessages() = true

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // Get the unanswered tag for the forum
        val unansweredTag = forum.availableTags.find { it.name == "unanswered" }
        if (unansweredTag == null) {
            println("Could not find 'unanswered' tag in $forum")
            return
        }

        val tags = LinkedHashSet(thread.appliedTags)

        // Message from OP, probably another question
        if (event.author.id == thread.ownerId) {
            tags.add(unansweredTag)
            thread.manager.setAppliedTags(tags).queue()
        }

        // Message from non OP, probably an answer
        else {
            tags.remove(unansweredTag)
            thread.manager.setAppliedTags(tags).queue()
        }
    }
}
