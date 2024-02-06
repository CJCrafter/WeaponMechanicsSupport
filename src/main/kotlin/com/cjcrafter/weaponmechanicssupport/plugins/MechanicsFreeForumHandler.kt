package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.gitbook.GitBookApi
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.db.DiscordOpenAiMapping
import com.cjcrafter.weaponmechanicssupport.listeners.thread.OpenAIAnswerListener
import com.cjcrafter.weaponmechanicssupport.listeners.thread.UpdateUnansweredListener

class MechanicsFreeForumHandler(
    openai: OpenAI,
    gitbook: GitBookApi,
    dbMapping: DiscordOpenAiMapping,
) : PluginForumHandler(
"mechanics-free-support",
    listOf(
        UpdateUnansweredListener(),
        OpenAIAnswerListener(openai, "asst_1TTP9rnSIROj9r8uhr5bloW8", dbMapping),
    ),
    """.*""".toRegex(),
)