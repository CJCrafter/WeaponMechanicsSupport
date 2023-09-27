package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProtocolLibUpdateScraperTest {

    private val scraper = ProtocolLibUpdateScraper()

    @Test
    fun `test update detected`() {
        val line = "[17:31:12] [Server thread/INFO]: [ProtocolLib] The updater found an update: 5.1.0 (Running 5.1.0-SNAPSHOT-654). Download at https://www.spigotmc.org/resources/protocollib.1997/"
        val result = scraper.scrape(line)
        assertNotNull(result)
        assertEquals(ProtocolLibUpdateScraper.getErrorMessage("5.1.0-SNAPSHOT-654", "5.1.0", "https://www.spigotmc.org/resources/protocollib.1997/"), result)
    }

    @Test
    fun `test no update detected`() {
        val line = "[17:31:12] [Server thread/INFO]: [ProtocolLib] No updates found."
        val result = scraper.scrape(line)
        assertNull(result)
    }

    @Test
    fun `test different log line`() {
        val line = "[17:31:12] [Server thread/INFO]: Server is running version 1.19.4"
        val result = scraper.scrape(line)
        assertNull(result)
    }
}