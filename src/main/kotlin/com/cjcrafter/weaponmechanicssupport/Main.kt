package com.cjcrafter.weaponmechanicssupport

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.command.GenerateNameCommand
import com.cjcrafter.weaponmechanicssupport.command.LockCommand
import com.cjcrafter.weaponmechanicssupport.listeners.CommandManager
import com.cjcrafter.weaponmechanicssupport.plugins.WeaponMechanicsForumHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                println("java -jar <jar>.jar <openai> <jda>")
            }

            val openai = OpenAI(args[0])
            val jda = JDABuilder.createDefault(args[1])
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build()

            // Plugin thread listeners
            jda.addEventListener(
                WeaponMechanicsForumHandler(openai),
            )

            // Commands
            jda.addEventListener(CommandManager(jda, setOf(
                LockCommand(), GenerateNameCommand(openai)
            )))

            jda.awaitReady()
        }
    }
}
