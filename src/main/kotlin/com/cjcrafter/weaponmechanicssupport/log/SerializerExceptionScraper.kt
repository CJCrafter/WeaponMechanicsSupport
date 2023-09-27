package com.cjcrafter.weaponmechanicssupport.log

class SerializerExceptionScraper : LogScraper {

    var currentError: MutableList<String>? = null
    var plugin: String? = null

    override fun scrape(line: String): String? {
        val (plugin, content) = LogScraper.stripPlugin(line) // strip away the time/plugin info

        // This happens when there is a "blank line" in console. This is either:
        //   1. A stacktrace from an error
        //   2. A message from another plugin
        //   3. A list exception from WeaponMechanics
        // So we should check if we are currently in a SerializerException, we
        // should save the line. Otherwise, skip.
        if (content == line) {
            currentError?.add(content)
            return null
        }

        // Found a new error! Start accumulating messages
        if (content.contains("A mistake was found in your configurations when making")) {
            currentError = mutableListOf(content)
            this.plugin = plugin
            return null
        }

        // Found the end of the error, let's return it
        if ((this.plugin != plugin || content.isBlank()) && currentError != null) {
            val result = """
                There is an error in one of your config files. Fix this error first:
                
                > ${currentError!!.joinToString("\n                > ")}
            """.trimIndent()

            this.plugin = null
            this.currentError = null
            return result
        }

        // Continue the existing error
        currentError?.add(content)
        return null
    }
}
