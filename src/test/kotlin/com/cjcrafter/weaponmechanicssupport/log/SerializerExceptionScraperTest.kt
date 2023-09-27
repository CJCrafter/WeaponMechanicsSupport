package com.cjcrafter.weaponmechanicssupport.log

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SerializerExceptionScraperTest {

    private lateinit var scraper: LogScraper

    @BeforeEach
    fun setUp() {
        scraper = SerializerExceptionScraper()
    }

    @Test
    fun `test found error`() {
        val console = """
            [17:47:09] [Server thread/INFO]: Starting GS4 status listener
            [17:47:09] [Query Listener #1/INFO]: Query running on 0.0.0.0:41718
            [17:47:09] [Craft Scheduler Thread - 0/INFO]: [LootChest] [0;32;1mThe plugin seems up to date.[m

            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] A mistake was found in your configurations when making 'SoundMechanic'
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Could not match config to any Sound
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Found value: BLOCK_NOTE_BLOCK_HAT
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Did you mean to use 'BLOCK_CLOTH_BREAK' instead of 'BLOCK_NOTE_BLOCK_HAT'?
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Sound Reference: https://github.com/WeaponMechanics/MechanicsMain/wiki/References#sounds
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Wiki: https://github.com/WeaponMechanics/MechanicsMain/wiki/SoundMechanic
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] Located in file 'plugins/WeaponMechanics/weapons/marksman_rifles/Kar98k.yml' at 'Kar98k.Firearm_Action.Open.Mechanics'
                Sound{sound=BLOCK_NOTE_BLOCK_HAT, volume=0.5, noise=0.1, listeners=Source{}}
                            ^
            [17:47:10] [Server thread/ERROR]: [WeaponMechanics] 
        """.trimIndent()

        val result = StringBuilder()
        for (line in console.split("\n")) {
            scraper.scrape(line)?.apply { result.append(this) }
        }

        val expected = """
            There is an error in one of your config files. Fix this error first:
            
            > A mistake was found in your configurations when making 'SoundMechanic'
            > Could not match config to any Sound
            > Found value: BLOCK_NOTE_BLOCK_HAT
            > Did you mean to use 'BLOCK_CLOTH_BREAK' instead of 'BLOCK_NOTE_BLOCK_HAT'?
            > Sound Reference: https://github.com/WeaponMechanics/MechanicsMain/wiki/References#sounds
            > Wiki: https://github.com/WeaponMechanics/MechanicsMain/wiki/SoundMechanic
            > Located in file 'plugins/WeaponMechanics/weapons/marksman_rifles/Kar98k.yml' at 'Kar98k.Firearm_Action.Open.Mechanics'
            >     Sound{sound=BLOCK_NOTE_BLOCK_HAT, volume=0.5, noise=0.1, listeners=Source{}}
            >                 ^
        """.trimIndent()

        assertEquals(expected, result.toString())
    }

    @Test
    fun `test no error in console`() {
        val console = """
            [18:34:46] [Server thread/INFO]: Starting minecraft server version 1.12.2
            [18:34:46] [Server thread/INFO]: Loading properties
            [18:34:46] [Server thread/INFO]: Default game type: SURVIVAL
            [18:34:46] [Server thread/INFO]: This server is running CraftBukkit version git-Spigot-79a30d7-f4830a1 (MC: 1.12.2) (Implementing API version 1.12.2-R0.1-SNAPSHOT)
            [18:34:46] [Server thread/INFO]: Using 4 threads for Netty based IO
            [18:34:46] [Server thread/INFO]: Server Ping Player Sample Count: 12
        """.trimIndent()

        for (line in console.split("\n")) {
            assertNull(scraper.scrape(line))
        }
    }
}