package com.cjcrafter.weaponmechanicssupport.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

abstract class CommandWrapper(
    val name: String,
    val description: String,
    val permission: Permission = Permission.ADMINISTRATOR,
) {
    open fun getSubcommands() = listOf<SubcommandData>()

    open fun getOptions() = listOf<OptionData>()

    abstract fun onCommand(event: SlashCommandInteractionEvent)

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CommandWrapper
        return name == other.name
    }

    final override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "CommandWrapper(name='$name', description='$description')"
    }
}