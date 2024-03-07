package com.cjcrafter.weaponmechanicssupport.db

/**
 * Wrapper for the SQL mappings table. See the [DiscordOpenAiMapping].
 *
 * @property discordId The Discord ID of the user.
 * @property openaiId The OpenAI ID of the user.
 * @property lastAccessed The last time the user accessed the thread.
 */
data class AssistantThreadData(
    val discordId: String,
    val openaiId: String,
    val lastAccessed: String,
)
