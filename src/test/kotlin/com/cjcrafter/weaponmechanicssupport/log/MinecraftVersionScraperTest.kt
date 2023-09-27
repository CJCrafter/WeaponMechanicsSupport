package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MinecraftVersionScraperTest {

    private val scraper = MinecraftVersionScraper()

    @Test
    fun `test supported version`() {
        val line = "[15:49:32 INFO]: Starting minecraft server version 1.19.4"
        val result = scraper.scrape(line)
        assertEquals(null, result) // Since version 1.19.4 is supported, expect null
    }

    @Test
    fun `test unsupported version with upgrade suggestion`() {
        val line = "[15:49:32 INFO]: Starting minecraft server version 1.19.3"
        val result = scraper.scrape(line)
        assertEquals("You're running version 1.19.3 which isn't supported. Consider upgrading to version 1.19.4.", result)
    }

    @Test
    fun `test unsupported version without minor version`() {
        val line = "[15:49:32 INFO]: Starting minecraft server version 1.20"
        val result = scraper.scrape(line)
        assertEquals("You're running version 1.20 which isn't supported. Consider upgrading to version 1.20.1.", result)
    }

    @Test
    fun `test unsupported version with downgrade suggestion`() {
        val line = "[15:49:32 INFO]: Starting minecraft server version 1.20.3" // Assuming there's no version > 1.20.1 supported
        val result = scraper.scrape(line)
        assertEquals("You're running version 1.20.3 which isn't supported. Consider downgrading to a supported version.", result)
    }

    @Test
    fun `test line without version info`() {
        val line = "[15:49:32 INFO]: Some other log line"
        val result = scraper.scrape(line)
        assertEquals(null, result) // Should return null since line doesn't contain version info
    }
}
