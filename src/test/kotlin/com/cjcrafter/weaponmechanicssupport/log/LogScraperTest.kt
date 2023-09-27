package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LogScraperTest {

    @Test
    fun `test strip log`() {
        val line = "[17:31:12] [Server thread/INFO]: Hello World"
        val result = LogScraper.strip(line)
        assertEquals("Hello World", result)
    }
}