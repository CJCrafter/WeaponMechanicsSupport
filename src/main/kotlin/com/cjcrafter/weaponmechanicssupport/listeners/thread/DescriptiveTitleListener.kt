package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage.Companion.toSystemMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toUserMessage
import com.cjcrafter.openai.chat.ChatRequest
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class DescriptiveTitleListener(
    val openai: OpenAI,
) : ThreadMessageListener {

    override fun onThreadMessage(forum: ForumChannel, thread: ThreadChannel, message: Message, event: MessageReceivedEvent) {

        // Make sure that this is the first message in the thread
        thread.retrieveStartMessage().queue({
            if (it == message)
                updateTitle(openai, thread, message)
        }, { /* ignore... message deleted */ })
    }

    companion object {
        fun updateTitle(
            openai: OpenAI,
            thread: ThreadChannel,
            message: Message
        ): String {
            // Ask ChatGPT to generate a better title.
            val request = ChatRequest(
                "gpt-3.5-turbo", mutableListOf(
                    "Read the given forum post and respond with a descriptive title for the post in the form of a question. Use English. Use sentence capitalization.".toSystemMessage(),
                    (thread.name + "\n\n" + message.contentRaw).take(400).toUserMessage()
                )
            )
            val response = openai.createChatCompletion(request)
            var title = response[0].message.content

            // ChatGPT just LOVES to put quotes around shit. Remove them if we find them
            title = title.trim('"')

            // Update the title to the new descriptive title
            println("Updated title ${thread.name} --> $title")
            thread.manager.setName(title).queue()
            return title
        }
    }
}
