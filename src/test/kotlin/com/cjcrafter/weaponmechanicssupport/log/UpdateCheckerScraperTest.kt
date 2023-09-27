package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateCheckerScraperTest {

    val scraper = UpdateCheckerScraper()

    @Test
    fun `test line without update checker`() {
        val line = "[15:49:32 INFO]: Some other log line"
        val result = scraper.scrape(line)
        assertEquals(null, result) // Should return null since line doesn't contain update checker
    }

    @Test
    fun `test line with update error`() {
        val log = """
            Error occurred while enabling WeaponMechanics v2.6.5 (Is it up to date?)
            java.lang.InternalError: java.io.IOException: Server returned HTTP response code: 403 for URL: https://api.spigotmc.org/legacy/update.php?resource=99913
        """.trimIndent()

        for (line in log.split("\n")) {
            val result = scraper.scrape(line)

            if (result != null) {
                val response = """
                    There was a connection issue with WeaponMechanics.
                    This occurs when you restart your server very often, or many people join the server.
                    Fixes:
                      1. Disable the `Update_Checker` in `config.yml`
                      2. Change ResourcePack [download link](https://gist.github.com/CJCrafter/43d88f6c653628469e2f479999550095) to a private link for only your server
                """.trimIndent()
                assertEquals(response, result)
                return
            }
        }

        fail<Void>("Should have found update error")
    }
}