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
            if (args.size != 3) {
                println("java -jar <jar>.jar <openai key> <jda key> <gitbook key>")
            }

            val openai = OpenAI(args[0])
            val gitbook = GitBookApi.builder().apiKey(args[2]).build()
            val jda = JDABuilder.createDefault(args[1])
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
