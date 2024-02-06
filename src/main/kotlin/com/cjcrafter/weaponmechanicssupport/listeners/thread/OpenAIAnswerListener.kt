package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.openai.Models
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.tool.functionTool
import com.cjcrafter.openai.threads.Thread
import com.cjcrafter.openai.threads.create
import com.cjcrafter.openai.threads.message.TextContent
import com.cjcrafter.openai.threads.message.ThreadUser
import com.cjcrafter.openai.threads.runs.ListRunsRequest
import com.cjcrafter.openai.threads.runs.MessageCreationDetails
import com.cjcrafter.openai.threads.runs.Run
import com.cjcrafter.openai.threads.runs.RunStatus
import com.cjcrafter.weaponmechanicssupport.db.AssistantThreadData
import com.cjcrafter.weaponmechanicssupport.db.DiscordOpenAiMapping
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.File
import java.util.concurrent.TimeUnit

class OpenAIAnswerListener(
    val openai: OpenAI,
    val assistantId: String,
    val dbMapping: DiscordOpenAiMapping,
): ThreadMessageListener {

    /**
     * When a user sends a message on somebody else's thread, we should let them
     * know that they should create a new thread. However, sometimes people are
     * answering questions for other people, so we don't want to spam them with
     * messages. This set keeps track of users who have been warned, so we don't
     * warn them again. This data is lost when the bot restarts, but that's fine.
     */
    private val warnedUsers = mutableSetOf<WarnedUser>()

    override fun allowAdminMessages() = true

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        // TODO: Add a tag to disable the AI on this thread

        // Get the cached openai thread with the history of messages, or create a new one
        val openaiThread = getOrCreate(thread)
        if (openaiThread == null) {
            thread.sendMessage("Sorry, but this thread seems to be dead. Please create a new thread.").queueAfter(5, TimeUnit.SECONDS)
            return
        }

        // Add this new message to the thread. Messages sent by the thread owner
        // will be marked as "asks," while messages sent by anyone else will be
        // marked as "says." This is to make it more clear who is asking the
        // question. For example, if an ADMIN says "do xyz," the Assistant will
        // have that message as context.
        val sender = message.member ?: return
        val asks = if (sender.id == thread.ownerId) "asks" else "says"
        openai.threads.messages(openaiThread).create {
            role(ThreadUser.USER)
            content("${message.member!!.effectiveName} $asks:\n${message.contentRaw}")
        }

        // If this message was not sent by the owner, we should warn the user
        // that they should create a new thread (if they haven't already been
        // warned)
        if (sender.id != thread.ownerId) {
            val warnedUser = WarnedUser(thread.id, sender.id)
            if (!warnedUsers.contains(warnedUser)) {
                warnedUsers.add(warnedUser)
                val warning = "Hey ${sender.asMention}, thanks for helping out! If you are trying to ask a question, " +
                        "please create a new thread (**Even if you are asking a similar question**). " +
                        "I'll help you out right away when you create a new thread!\n" +
                        "(*I'll delete this message in 20 seconds*)"
                thread.sendMessage(warning).queueAfter(3, TimeUnit.SECONDS) { it.delete().queueAfter(20, TimeUnit.SECONDS) }
            }
            return
        }

        // Get the latest run. If this thread is already running, we have to stop.
        val runs = openai.threads.runs(openaiThread).list(ListRunsRequest.builder().limit(1).build())
        val latestRun = runs.data.getOrNull(0)
        if (latestRun != null && !latestRun.status.isTerminal && latestRun.status != RunStatus.REQUIRED_ACTION)
            return

        // Use GPT4 for the first message, then use GPT3 for the rest
        val useGPT4: Boolean = (latestRun == null)

        // At this point, we know that the thread owner is asking a question. So
        // we need to create a new run for the thread.
        val run = openai.threads.runs(openaiThread).create {
            assistantId(assistantId)
            model(if (useGPT4) Models.Chat.GPT_4_1106_PREVIEW else "gpt-3.5-turbo-0125")

            // GPT4 is PLENTY powerful, so it doesn't need any tools
            if (!useGPT4) {
                tools(mutableListOf(
                    functionTool {
                        name("ask_question")
                        val desc = "A human-readable question for the wiki without extra context. Example: Can I use left click to shoot?"
                        addStringParameter("question", desc, required = true)
                    }
                ))
            }
        }

        // Create a non-blocking thread to handle the run's terminal status
        handleRunTermination(message, openaiThread, run)
    }

    /**
     * At the time of writing, the OpenAI API is quite limited for Assistants.
     * In order for to check if a run is complete, we have to wait for the run
     * to complete, and then retrieve the steps of the run.
     *
     * We create a new [java.lang.Thread] so we do not block other actions.
     */
    fun handleRunTermination(message: Message, openaiThread: Thread, run: Run) {
        Thread {
            // Wait for the run to finish
            var run = openai.threads.runs(openaiThread).retrieve(run)
            while (!run.status.isTerminal) {
                java.lang.Thread.sleep(1000)
                run = openai.threads.runs(openaiThread).retrieve(run)
            }

            // If the run failed, we should let the user know
            if (run.status == RunStatus.FAILED) {
                println("Some error occurred when trying to reply to '${message.contentRaw}'")
                println("Last error: ${run.lastError}")
                return@Thread
            }

            val responseData = ResponseData()

            // If the run was successful, we should retrieve the steps of the run
            val steps = openai.threads.runs(openaiThread).steps(run).list()
            for (step in steps.data) {
                when (val details = step.stepDetails) {
                    is MessageCreationDetails -> {
                        val messageId = details.messageCreation.messageId
                        val assistantMessage = openai.threads.messages(openaiThread).retrieve(messageId)
                        for (content in assistantMessage.content) {
                            when (content) {
                                is TextContent -> responseData.messages.add(content.text.value)

                                // We only expect this bot to output text
                                else -> {
                                    println("Unexpected content type: $content")
                                }
                            }
                        }
                    }

                    // We only expect the bot to create messages, no tool usage.
                    else -> {
                        println("Unexpected step details: $details")
                    }
                }
            }

            // Condense the messages into 1 message, and send it to the thread.
            var response = responseData.messages.joinToString("\n")
            if (responseData.sources.isNotEmpty()) {
                response += "\n\nSources:\n" + responseData.sources.joinToString("\n")
            }

            // This data is valuable AF for making new AI models, and can help
            // me to save money when I want to produce better models to help
            // more WM users. So this data is saved in a .json file.
            appendToJsonFile("${openaiThread.id}.json", message.contentRaw, response)

            message.reply(response).queue()

        }.start()
    }

    /**
     * Attempts to get the cached OpenAI thread for the given discord thread. If
     * the thread does not exist, a new one will be created.
     *
     * @param thread The discord thread associated with the OpenAI thread
     * @return The OpenAI thread, or null if something went wrong
     */
    fun getOrCreate(thread: ThreadChannel): Thread? {
        val cached: AssistantThreadData? = dbMapping.getMapping(thread.id)

        try {
            return if (cached != null) {
                openai.threads.retrieve(cached.openaiId)
            } else {
                val openaiThread = openai.threads.create {
                    addMetadata("discord_thread_owner", thread.ownerId)
                    addMetadata("discord_thread_id", thread.id)
                }
                dbMapping.addMapping(thread.id, openaiThread.id)
                openaiThread
            }
        } catch (ex: Throwable) {
            println("Some error occurred when trying to retrieve/create a thread for '${thread.name}'")
            println("Error: ${ex.message}")
            ex.printStackTrace()
            return null
        }
    }

    fun appendToJsonFile(fileName: String, question: String, response: String) {
        if (!fileName.endsWith(".json"))
            throw IllegalArgumentException("The file name must end with '.json'")

        val folder = File("training").apply { mkdirs() }
        val file = folder.resolve(fileName)
        if (!file.exists())
            file.createNewFile()

        // Load json from the file, or create a new json object if the file is empty
        val jsonMapper = jacksonObjectMapper()
        var json: JsonNode = jsonMapper.readTree(file)
        if (json !is ObjectNode)
            json = jsonMapper.createObjectNode()

        // Append the new data to the json file
        val messages = json.get("messages") as? ArrayNode ?: jsonMapper.createArrayNode()
        messages.add(jsonMapper.createObjectNode().apply {
            put("role", "user")
            put("content", question)
        })
        messages.add(jsonMapper.createObjectNode().apply {
            put("role", "assistant")
            put("content", response)
        })

        // Save the new json to the file
        json as ObjectNode
        json.replace("messages", messages)
        jsonMapper.writeValue(file, json)
    }

    /**
     * Stores a generated message.
     *
     * @property messages The messages generated by the AI
     * @property sources The sources used to generate the messages
     */
    private data class ResponseData(val messages: MutableList<String> = mutableListOf(), val sources: MutableList<String> = mutableListOf())

    /**
     * This class is used as a key in a hashmap.
     *
     * @param threadId The thread which the user was warned on
     * @param userId The user who was warned
     */
    private data class WarnedUser(val threadId: String, val userId: String)
}