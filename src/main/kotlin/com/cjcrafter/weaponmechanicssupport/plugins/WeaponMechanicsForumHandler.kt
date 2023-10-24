package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatUsage
import com.cjcrafter.weaponmechanicssupport.listeners.thread.*

class WeaponMechanicsForumHandler(openai: OpenAI, gitbook: GitBookApi) : PluginForumHandler(
    "weaponmechanics-support",
    listOf(
        DescriptiveTitleListener(openai),
        TranslateMessageListener(openai),
        GitbookAnswerListener(gitbook),
        UpdateUnansweredListener(),
        LockedThreadListener(),
        ScrapeLogListener(),
        YamlFileListener(),
        IncompleteLogListener(),
        IncompleteYamlListener(),
    )
)