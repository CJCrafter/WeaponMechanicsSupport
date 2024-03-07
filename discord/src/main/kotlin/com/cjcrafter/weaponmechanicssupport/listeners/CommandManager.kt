package com.cjcrafter.weaponmechanicssupport.listeners

import com.cjcrafter.weaponmechanicssupport.command.CommandWrapper
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import java.awt.Color
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CommandManager(
    private val jda: JDA,
    commands: Set<CommandWrapper>
) : ListenerAdapter() {

    private val pool: ExecutorService = Executors.newCachedThreadPool()
    private val commands: Set<CommandWrapper> = ConcurrentHashMap.newKeySet<CommandWrapper>().apply {
        addAll(commands)
    }

    init {
        commands.forEach { register(it) }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild)
            return

        for (commandWrapper in commands) {
            if (event.name.equals(commandWrapper.name, ignoreCase = true)) {
                tryCommand(commandWrapper, event)
                break
            }
        }
    }

    private fun register(commandWrapper: CommandWrapper) {
        val command = jda.upsertCommand(commandWrapper.name, commandWrapper.description)
        command.setGuildOnly(true)
        command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(commandWrapper.permission))

        // Arguments/subcommands
        command.addSubcommands(commandWrapper.getSubcommands())
        command.addOptions(commandWrapper.getOptions())

        // Registers the command to JDA
        println("Registering command ${command.name}")
        command.queue()
    }

    fun tryCommand(commandWrapper: CommandWrapper, event: SlashCommandInteractionEvent) {
        pool.submit {
            try {

                if (!event.member!!.hasPermission(commandWrapper.permission)) {
                    val embed = EmbedBuilder()
                        .setColor(Color.RED)
                        .setDescription("Sorry, ${event.user.asMention}, you do not have permission to use this command.")
                        .setFooter(jda.selfUser.name, jda.selfUser.avatarUrl)
                        .setTimestamp(Instant.now())
                        .build()

                    event.replyEmbeds(embed).setEphemeral(true).queue()
                    return@submit
                }

                // Execute the command
                commandWrapper.onCommand(event)

            } catch (ex: Throwable) {
                if (!event.interaction.isAcknowledged)
                    event.deferReply().setContent("An error occurred while processing your command!").setEphemeral(true).queue()
                else
                    event.hook.editOriginal("An error occurred while processing your command!").queue()

                println("Some error occurred in command ${commandWrapper.name} by ${event.user.name}")
                ex.printStackTrace()
            }
        }
    }
}