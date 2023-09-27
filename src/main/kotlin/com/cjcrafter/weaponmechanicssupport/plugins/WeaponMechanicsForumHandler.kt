package com.cjcrafter.weaponmechanicssupport.plugins

import com.cjcrafter.openai.OpenAI
import com.cjcrafter.weaponmechanicssupport.listeners.thread.*

class WeaponMechanicsForumHandler(openai: OpenAI) : PluginForumHandler(
    "weaponmechanics-support",
    listOf(
        DescriptiveTitleListener(openai),
        UpdateUnansweredListener(),
        LockedThreadListener(),
        ScrapeLogListener(),
        YamlFileListener(),
        IncompleteLogListener(),
        IncompleteYamlListener(),
    )
)