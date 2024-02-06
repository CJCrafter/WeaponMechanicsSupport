package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.db.DiscordOpenAiMapping
import com.cjcrafter.weaponmechanicssupport.listeners.thread.*

class WeaponMechanicsForumHandler(
    openai: OpenAI,
    gitbook: GitBookApi,
    dbMapping: DiscordOpenAiMapping,
) : PluginForumHandler(
    "weaponmechanics-support",
    listOf(
        DescriptiveTitleListener(openai),
        TranslateMessageListener(openai),
        GitbookAnswerListener(gitbook, "weaponmechanics"),
        UpdateUnansweredListener(),
        LockedThreadListener(),
        ScrapeLogListener(),
        YamlFileListener(),
        IncompleteLogListener(),
        IncompleteYamlListener(),
    )
)