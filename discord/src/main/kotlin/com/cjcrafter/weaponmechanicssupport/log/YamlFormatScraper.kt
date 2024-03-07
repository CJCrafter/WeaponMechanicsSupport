package com.cjcrafter.weaponmechanicssupport.log

class YamlFormatScraper : LogScraper {

    var file: String? = null
    val supportedPlugins = listOf(
        "WeaponMechanics",
        "MechanicsCore",
        "BiomeManager",
        "WeaponMechanicsSignShop",
        "ArmorMechanics",
        "WeaponMechanicsPlus",
        "WeaponMechanicsCosmetics"
    )

    override fun scrape(line: String): String? {
        if (file != null && line.startsWith("org.bukkit.configuration.InvalidConfigurationException")) {
            return "It seems there's a YAML format error in $file. Please upload your $file file, and I can help you find exactly where your error is!"
        }

        file = if (supportedPlugins.any { line.contains("Cannot load plugins/$it") })
            line.split(" ").last()
        else
            null

        return null
    }
}