package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage.Companion.toSystemMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toUserMessage
import com.cjcrafter.openai.chat.ChatRequest
import com.cjcrafter.openai.chat.chatRequest
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class TranslateMessageListener(
    private val openai: OpenAI
): ThreadMessageListener {

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        val translation = translateMessage(openai, message.contentRaw) ?: return
        message.reply("""
            ### Translation
            *$translation*
            """.trimIndent()).queue()
    }

    companion object {
        @JvmStatic
        fun translateMessage(openai: OpenAI, message: String): String? {
            // TODO do not modify YAML
            val request = chatRequest {
                model("gpt-3.5-turbo")
                addMessage("If the phrase given is not in English, translate it into English. Otherwise, reply \"already english\".".toSystemMessage())
                addMessage(message.take(1000).toUserMessage())

                topP(0.25f)
                temperature(0.8f)
            }

            val response = openai.createChatCompletion(request)
            val translation = response.choices[0].message.content!!

            val translationStripped = translation.lowercase().trim().trim('"')
            if (translationStripped == "already english" || translationStripped.startsWith("i'm sorry, but"))
                return null

            return translation
        }
    }
}