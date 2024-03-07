package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class YamlFormatScraperTest {

    private val scraper = YamlFormatScraper()

    @Test
    fun `test line without yaml format error`() {
        val line = "[15:49:32 INFO]: Some other log line"
        val result = scraper.scrape(line)
        assertEquals(null, result) // Should return null since line doesn't contain yaml format error
    }

    @Test
    fun `test log with yaml format error`() {
        val log = """
            [19:45:41] [Server thread/ERROR]: Cannot load plugins/WeaponMechanics/ammos/Default_Ammos.yml
            org.bukkit.configuration.InvalidConfigurationException: while scanning for the next token
            found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
             in 'string', line 3, column 5:
        """.trimIndent()

        for (line in log.split("\n")) {
            val result = scraper.scrape(line)

            if (result != null) {
                assertEquals("It seems there's a YAML format error in plugins/WeaponMechanics/ammos/Default_Ammos.yml. Please upload your plugins/WeaponMechanics/ammos/Default_Ammos.yml file, and I can help you find exactly where your error is!", result)
                return
            }
        }

        fail<Void>("Should have found yaml format error")
    }
}