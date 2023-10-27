package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.gitbook.AskRequest
import com.cjcrafter.gitbook.GitBookApi
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color

class GitbookAnswerListener(
    private val gitbook: GitBookApi
): ThreadMessageListener {

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // Make sure that this is the first message in the thread
        thread.retrieveStartMessage().queue({
            if (it != message)
                return@queue

            val answer = answer(gitbook, it.contentRaw) ?: return@queue
            message.replyEmbeds(answer.first).apply {
                if (answer.second.isNotEmpty())
                    this.setActionRow(answer.second)
            }.queue()

        }, { /* ignore... message deleted */ })
    }

    companion object {
        @JvmStatic
        fun answer(gitbook: GitBookApi, question: String, previousQuestions: List<String> = listOf()): Pair<MessageEmbed, List<Button>>? {
            val request = AskRequest(query = question, previousQueries = previousQuestions)
            val response = gitbook.ask(request)

            println("Asking gitbook: $question (previous questions = ${previousQuestions.joinToString(", ")}))")

            val answer = response.getOrNull()?.answer ?: return null
            val formattedAnswer = """
                > ${question.split("\n").joinToString("\n> ")}
                
                ${answer.text}
                
                *I am a bot, and I often make mistakes.* Use `/ask` to ask another question.
                """.trimIndent()

            println("${question.replace("\n", "\\n")} -> ${answer.text.replace("\n", "\\n")}")

            val embed = EmbedBuilder()
                .setTitle("You asked...")
                .setDescription(formattedAnswer)
                .setColor(Color(0, 0, 0))
                .build()

            val previousQueries = previousQuestions.toMutableList().apply { add(question) }
            val buttons = answer.followupQuestions.mapIndexed { index, s -> Button.secondary("gitbook_${index}_${previousQueries.joinToString("|")}", s) }
            return Pair(embed, buttons)
        }
    }
}
