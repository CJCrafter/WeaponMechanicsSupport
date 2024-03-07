package com.cjcrafter.weaponmechanicssupport.listeners.thread

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class IncompleteLogListenerTest {

    @ParameterizedTest
    @MethodSource("provide_testNormalDiscordMessage")
    fun `test normal discord message`(content: String) {
        val isLog = IncompleteLogListener.isIncompleteLog(content)
        assertFalse(isLog)
    }

    @ParameterizedTest
    @MethodSource("provide_testLogLine")
    fun `test log line`(content: String) {
        val isLog = IncompleteLogListener.isIncompleteLog(content)
        assertTrue(isLog)
    }

    @ParameterizedTest
    @MethodSource("provide_testLogError")
    fun `test log error`(content: String) {
        val isLog = IncompleteLogListener.isIncompleteLog(content)
        assertTrue(isLog)
    }

    companion object {
        @JvmStatic
        fun provide_testNormalDiscordMessage(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("Hello, world!"),
                Arguments.of("at what point"),
            )
        }

        @JvmStatic
        fun provide_testLogLine(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("[12:34:56] [Server thread/INFO]: Hello, world!"),
                Arguments.of("[19:44:33] [Server thread/INFO]: [LuckPerms] Loading configuration..."),
                Arguments.of("[19:44:48] [Server thread/INFO]: Preparing spawn area: 0%"),
                Arguments.of("""
                    [07:35:59 ERROR]: [WeaponMechanics] A mistake was found in your configurations when making 'AmmoTypes'
                    [07:35:59 ERROR]: [WeaponMechanics] Tried to use ammo without any options? You should use at least one of the ammo types!
                    [07:35:59 ERROR]: [WeaponMechanics] Located in file 'plugins/WeaponMechanics/ammos/Default_Ammos.yml' at 'Basic_Ammo.Ammo_Types.High_Explosive'
                    [07:35:59 ERROR]: [WeaponMechanics] 
                """.trimIndent())
            )
        }

        @JvmStatic
        fun provide_testLogError(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("""
                    at org.bukkit.craftbukkit.v1_16_R3.CraftServer.loadPlugins(CraftServer.java:393) ~[patched_1.16.5.jar:git-Paper-782]
                    at net.minecraft.server.v1_16_R3.DedicatedServer.init(DedicatedServer.java:269) ~[patched_1.16.5.jar:git-Paper-782]
                    at net.minecraft.server.v1_16_R3.MinecraftServer.w(MinecraftServer.java:1069) ~[patched_1.16.5.jar:git-Paper-782]
                    at net.minecraft.server.v1_16_R3.MinecraftServer.lambda${'$'}a${'$'}0(MinecraftServer.java:291) ~[patched_1.16.5.jar:git-Paper-782]
                    at java.lang.Thread.run(Thread.java:831) [?:?]
                """.trimIndent()),
                Arguments.of("""
                    Caused by: org.yaml.snakeyaml.parser.ParserException: while parsing a block mapping
                     in 'string', line 353, column 7:
                              Format: "&9[{player}] &f%fmeria_ ... 
                              ^
                    expected <block end>, but found '<scalar>'
                     in 'string', line 353, column 78:
                         ... &f{displayname} &7: &f{message}"" #The format you want the group ... 
                """.trimIndent()),
                Arguments.of("""
                    Cannot load plugins/WeaponMechanics/ammos/Default_Ammos.yml
                    org.bukkit.configuration.InvalidConfigurationException: while scanning for the next token
                    found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
                     in 'string', line 3, column 5:
                            	Item_Ammo:
                """.trimIndent())
            )
        }
    }
}