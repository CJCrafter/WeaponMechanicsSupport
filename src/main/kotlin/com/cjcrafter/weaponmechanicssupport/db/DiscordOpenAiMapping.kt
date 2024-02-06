package com.cjcrafter.weaponmechanicssupport.db

import java.sql.Connection
import java.sql.DriverManager

class DiscordOpenAiMapping {

    private val connection: Connection = DriverManager.getConnection("jdbc:h2:./openai_mappings", "sa", "")

    init {
        // Create the connection to the database. The .db file is in the working
        // directory (Usually the same directory as the .jar)
        createTable()
    }

    /**
     * Creates the mapping table if it does not exist.
     */
    private fun createTable() {
        val stmt = connection.createStatement()
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS mapping (
                discord_id VARCHAR(255),
                openai_id VARCHAR(255),
                last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
            )
        """.trimIndent())
        stmt.close()
    }

    /**
     * Adds a mapping to the database.
     *
     * @param discordId The Discord ID of the user.
     * @param openaiId The OpenAI ID of the user.
     */
    fun addMapping(discordId: String, openaiId: String) {
        val preparedStatement = connection.prepareStatement("""
            INSERT INTO mapping (discord_id, openai_id, last_accessed) VALUES (?, ?, CURRENT_TIMESTAMP())
        """.trimIndent())

        preparedStatement.setString(1, discordId)
        preparedStatement.setString(2, openaiId)
        preparedStatement.executeUpdate()
        preparedStatement.close()
    }

    /**
     * Updates the last accessed time for a mapping.
     *
     * @param discordId The Discord ID of the user.
     */
    fun getMapping(discordId: String): AssistantThreadData? {
        val preparedStatement = connection.prepareStatement("""
            SELECT * FROM mapping WHERE discord_id = ?
        """.trimIndent())

        preparedStatement.setString(1, discordId)
        val resultSet = preparedStatement.executeQuery()
        if (resultSet.next()) {

            // Update the last accessed time
            val updateStatement = connection.prepareStatement("""
                UPDATE mapping SET last_accessed = CURRENT_TIMESTAMP() WHERE discord_id = ?
            """.trimIndent())
            updateStatement.setString(1, discordId)
            updateStatement.executeUpdate()
            updateStatement.close()

            return AssistantThreadData(
                resultSet.getString("discord_id"),
                resultSet.getString("openai_id"),
                resultSet.getString("last_accessed")
            )
        }
        resultSet.close()
        preparedStatement.close()
        return null
    }
}
