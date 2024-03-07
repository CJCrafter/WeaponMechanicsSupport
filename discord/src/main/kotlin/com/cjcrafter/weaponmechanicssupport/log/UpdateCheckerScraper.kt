package com.cjcrafter.weaponmechanicssupport.log

class UpdateCheckerScraper : LogScraper {

    private var plugin: String? = null
    private val supportedPlugins = listOf(
        "WeaponMechanics",
        "ArmorMechanics",
        "WeaponMechanicsPlus",
        "WeaponMechanicsCosmetics"
    )

    override fun scrape(line: String): String? {
        supportedPlugins.find { line.contains("Error occurred while enabling $it") }?.let {
            plugin = it
            return null
        }

        if (plugin != null && line.contains("java.lang.InternalError: java.io.IOException: Server returned HTTP response code")) {
            val response = """
                |There was a connection issue with $plugin.
                |This occurs when you restart your server very often, or many people join the server.
                |Fixes:
                |  1. Disable the `Update_Checker` in `config.yml`
                |  2. Change ResourcePack [download link](https://gist.github.com/CJCrafter/43d88f6c653628469e2f479999550095) to a private link for only your server
            """.trimMargin()
            plugin = null
            return response
        }

        plugin = null
        return null
    }

}