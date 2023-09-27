package com.cjcrafter.weaponmechanicssupport.listeners.thread

import com.cjcrafter.weaponmechanicssupport.log.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class ScrapeLogListener : ThreadMessageListener {

    // Order is important... put higher priority scrapers first
    private val scraperTypes = listOf(
        JavaVersionScraper::class.java,
        YamlFormatScraper::class.java,
        MinecraftVersionScraper::class.java,
        UpdateCheckerScraper::class.java,
        SerializerExceptionScraper::class.java,
        ProtocolLibUpdateScraper::class.java,
    )

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        val log = message.attachments.find { it.fileExtension == "log" } ?: return

        // Instantiate our scrapers
        val scrapers = scraperTypes.map { it.getDeclaredConstructor().newInstance() }

        // Download the log file content
        val future = log.proxy.download()
        future.thenAccept { inputStream ->
            inputStream.bufferedReader().use { reader ->

                val errorMessages = mutableListOf<String>()

                reader.forEachLine { line ->
                    // Limit errors to avoid long iterations
                    if (errorMessages.size > 50)
                        return@forEachLine
                    scrapers.forEach { scraper ->
                        scraper.scrape(line)?.let { errorMessages.add(it) }
                    }
                }

                val builder =
                    StringBuilder("I looked through your console, and I found some issues for you to fix:\n\n")
                var displayedErrors = 0

                errorMessages.forEachIndexed { index, error ->
                    val messageToAdd = "# Problem ${index + 1}\n$error\n"

                    // Check if appending the message would exceed the limit
                    if (builder.length + messageToAdd.length < 1950) { // 1950 to keep some buffer
                        builder.append(messageToAdd)
                        displayedErrors++
                    }
                }

                if (errorMessages.size > displayedErrors) {
                    builder.append("\n_I also found ${errorMessages.size - displayedErrors} other errors. Check the attached file for details.")
                }

                // Only respond to the log if there were errors
                if (displayedErrors > 0) {
                    val reply = message.reply(builder.toString())
                    if (errorMessages.size > displayedErrors)
                        reply.addFiles(FileUpload.fromData(generateErrorStream(errorMessages), "errors.txt"))
                    reply.queue()
                }
            }

        }.exceptionally { throwable ->
            // Handle the exception
            println("Failed to process the log file due to an error: ${throwable.message}")
            throwable.printStackTrace()
            null
        }
    }

    fun generateErrorStream(errors: List<String>): InputStream {
        val baos = ByteArrayOutputStream()
        baos.bufferedWriter().use { writer ->
            errors.forEach { writer.write("$it\n\n") }
        }
        return ByteArrayInputStream(baos.toByteArray())
    }
}
