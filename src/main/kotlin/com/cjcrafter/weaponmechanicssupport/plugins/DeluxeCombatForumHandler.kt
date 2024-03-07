package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.listeners.thread.*

class DeluxeCombatForumHandler(openai: OpenAI, gitbook: GitBookApi) : PluginForumHandler(
    "deluxecombat-support",
    listOf(
        TranslateMessageListener(openai),
        GitbookAnswerListener(gitbook, "deluxecombat"),
        UpdateUnansweredListener(),
        YamlFileListener(),
        IncompleteLogListener(),
        IncompleteYamlListener(),
    ),
    "question|bug|feature request".toRegex()
)