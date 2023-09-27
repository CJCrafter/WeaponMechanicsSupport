package com.cjcrafter.weaponmechanicssupport.listeners.thread

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class IncompleteYamlListenerTest {

    @ParameterizedTest
    @MethodSource("provide_testMessageWithoutYaml")
    fun `test message without yaml`(content: String) {
        val hasYaml = IncompleteYamlListener.hasYaml(content)
        assertFalse(hasYaml)
    }

    @ParameterizedTest
    @MethodSource("provide_testMessageWithCodeBlock")
    fun `test message with code block`(content: String) {
        val hasYaml = IncompleteYamlListener.hasYaml(content)
        assertFalse(hasYaml)
    }

    @ParameterizedTest
    @MethodSource("provide_testMessageWithIncompleteYaml")
    fun `test message with incomplete yaml`(content: String) {
        val hasYaml = IncompleteYamlListener.hasYaml(content)
        assertTrue(hasYaml)
    }

    companion object {
        @JvmStatic
        fun provide_testMessageWithoutYaml(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("This is a message without yaml"),
                Arguments.of("This is a message without yaml: true"),
                Arguments.of("This is a message without yaml: true\nThis is a message without yaml: true"),
            )
        }

        @JvmStatic
        fun provide_testMessageWithCodeBlock(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("""
                    ```yaml
                    This_Yaml: "is valid"
                    ```
                """.trimIndent()),
                Arguments.of("""
                    Mechanics:
                      - "Push{} @Target{}"
                """.trimIndent()),
                Arguments.of("""
                    ```yaml
                      Projectile: <path to another Projectile key>
                        Projectile_Settings:
                    ```
                """.trimIndent())
            )
        }

        @JvmStatic
        fun provide_testMessageWithIncompleteYaml(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("""        
                    this no work
                    commonbalammo:
                      Ammo_Types:
                        High_Explosive1:
                          Item_Ammo:
                            Magazine_Item:
                              Type: "FEATHER"
                              Name: "Common Ballistic Magazine"
                              Lore:
                              - "A simple sing stack magazine for common ballistic firearms"
                            Custom_Model_Data: 2
                          Ammo_Converter_Check:
                            Type: true
                            Name: true
                            Lore: true
                            Enchants: false
                    
                    Wa-wa
                """.trimIndent()),
                Arguments.of("""
                    Hello:
                      There: "pause for effect" 
                      General: Kenobi
                """.trimIndent())
            )
        }
    }
}
