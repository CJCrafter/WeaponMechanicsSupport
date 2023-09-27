package com.cjcrafter.weaponmechanicssupport.listeners.thread

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.error.YAMLException
import java.io.InputStream

class YamlFileListener : ThreadMessageListener {

    override fun onThreadMessage(
        forum: ForumChannel,
        thread: ThreadChannel,
        message: Message,
        event: MessageReceivedEvent
    ) {
        for (attachment in message.attachments) {
            // Optional: Check for .yml or .yaml extension before attempting to parse.
            if (attachment.fileName.endsWith(".yml", ignoreCase = true) || attachment.fileName.endsWith(".yaml", ignoreCase = true)) {
                attachment.proxy.download().thenAccept { contentStream ->
                    parseYaml(contentStream, message)
                }.exceptionally {
                    event.channel.sendMessage("I'm sorry, but I couldn't download ${attachment.fileName}... My internet might be down or your file might be corrupted.").queue()
                    null
                }
            }
        }

        // We can also try to parse ```yaml blocks for formatting errors
        val regex = """```yaml\n(.*?)```""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val yaml = regex.find(message.contentRaw)?.groupValues?.get(1)
        if (yaml != null) {
            parseYaml(yaml.byteInputStream(), message)
        }
    }

    private fun parseYaml(content: InputStream, message: Message) {
        val contentString = content.bufferedReader().readText()
        try {
            val yaml = Yaml()
            yaml.load<Map<String, Any>>(contentString.reader())

        } catch (e: MarkedYAMLException) {
            // Extract line and column info
            val errorLine = e.problemMark?.line ?: -1

            // Extract problematic line from contentString for better clarity, if available
            val lines = contentString.lines()
            val startLine = maxOf(0, errorLine - 2)
            val endLine = minOf(lines.size - 1, errorLine + 2)

            val userFriendlyMessage = buildString {
                append("Error parsing the YAML file on line ${errorLine + 1}: ${e.problem}\n")
                if (errorLine != -1) {
                    append("```yaml\n")
                    for (i in startLine..endLine) {
                        append(lines[i])
                        if (i == errorLine)
                            append("  # <- Error here")
                        append("\n")
                    }
                    append("\n```\n")
                }
                append("\nTips:\n")
                append("- Ensure proper indentation using spaces, not tabs (And make sure each indent is 2 spaces!).\n")
                append("- Check for missing colons `:` or dashes `-`.\n")
                append("- Ensure strings with special characters are enclosed in quotes.")
            }
            message.reply(userFriendlyMessage).queue()
        } catch (e: YAMLException) {
            message.reply("There was an issue with your YAML but I couldn't figure out what...").queue()
            e.printStackTrace()
        } catch (e: Exception) {
            message.reply("An unknown error occurred... ${e.message}").queue()
            e.printStackTrace()
        }
    }
}
