package com.cjcrafter.weaponmechanicssupport

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.command.AskCommand
import com.cjcrafter.weaponmechanicssupport.command.GenerateNameCommand
import com.cjcrafter.weaponmechanicssupport.command.LockCommand
import com.cjcrafter.weaponmechanicssupport.command.TranslateCommand
import com.cjcrafter.weaponmechanicssupport.listeners.ButtonClickHandler
import com.cjcrafter.weaponmechanicssupport.listeners.CommandManager
import com.cjcrafter.weaponmechanicssupport.plugins.WeaponMechanicsForumHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

class Main {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val openai = OpenAI(System.getenv("openai_key") ?: System.getenv("OPENAI_KEY"))
            val gitbook = GitBookApi.builder().apiKey(System.getenv("gitbook_key") ?: System.getenv("GITBOOK_KEY")).build()
            val jda = JDABuilder.createDefault(System.getenv("discord_key") ?: System.getenv("DISCORD_KEY"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build()

            // Plugin thread listeners
            jda.addEventListener(
                WeaponMechanicsForumHandler(openai, gitbook),
                ButtonClickHandler(gitbook)
            )

            // Commands
            jda.addEventListener(CommandManager(jda, setOf(
                LockCommand(), GenerateNameCommand(openai), TranslateCommand(openai), AskCommand(gitbook)
            )))

            jda.awaitReady()
        }
    }
}
