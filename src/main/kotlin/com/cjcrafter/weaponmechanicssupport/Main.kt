package com.cjcrafter.weaponmechanicssupport

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.openAI
import com.cjcrafter.weaponmechanicssupport.command.AskCommand
import com.cjcrafter.weaponmechanicssupport.command.GenerateNameCommand
import com.cjcrafter.weaponmechanicssupport.command.LockCommand
import com.cjcrafter.weaponmechanicssupport.command.TranslateCommand
import com.cjcrafter.weaponmechanicssupport.db.DiscordOpenAiMapping
import com.cjcrafter.weaponmechanicssupport.listeners.ButtonClickHandler
import com.cjcrafter.weaponmechanicssupport.listeners.CommandManager
import com.cjcrafter.weaponmechanicssupport.plugins.MechanicsFreeForumHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

class Main {
    companion object {

        val gitbooks: MutableMap<String, GitBookApi> = mutableMapOf()

        @JvmStatic
        fun main(args: Array<String>) {
            val openai = openAI { apiKey(System.getenv("openai_key") ?: System.getenv("OPENAI_KEY")) }
            val gitbook = GitBookApi.builder().apiKey(System.getenv("gitbook_key") ?: System.getenv("GITBOOK_KEY")).build()
            val deluxeCombatGitbook = GitBookApi.builder().apiKey(System.getenv("deluxecombat_key") ?: System.getenv("DELUXECOMBAT_KEY")).build()
            val jda = JDABuilder.createDefault(System.getenv("discord_key") ?: System.getenv("DISCORD_KEY"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build()

            gitbooks["weaponmechanics"] = gitbook
            gitbooks["deluxecombat"] = deluxeCombatGitbook

            val dbMapping = DiscordOpenAiMapping()

            // Plugin thread listeners
            jda.addEventListener(
                MechanicsFreeForumHandler(openai, gitbook, dbMapping),
                //WeaponMechanicsForumHandler(openai, gitbook, dbMapping),
                //DeluxeCombatForumHandler(openai, deluxeCombatGitbook),
                ButtonClickHandler(),
            )

            // Commands
            jda.addEventListener(CommandManager(jda, setOf(
                LockCommand(), GenerateNameCommand(openai), TranslateCommand(openai), AskCommand()
            )))

            jda.awaitReady()
        }
    }
}
