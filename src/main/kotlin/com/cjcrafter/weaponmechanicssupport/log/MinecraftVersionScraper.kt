package com.cjcrafter.weaponmechanicssupport.log

class MinecraftVersionScraper : LogScraper {

    private val supportedVersions = listOf(
        "1.12.2",
        "1.13.2",
        "1.14.4",
        "1.15.2",
        "1.16.5",
        "1.17.1",
        "1.18.2",
        "1.19.4",
        "1.20.1"
    )

    override fun scrape(line: String): String? {
        // Every log file has a line near the top like:
        // [15:49:32 INFO]: Starting minecraft server version 1.19.3
        // We can extract the server version from this.

        if (!line.contains("Starting minecraft server version")) return null

        // Extracting the server version from the line
        val versionPattern = """version (\d+\.\d+\.?\d*)""".toRegex()
        val matchResult = versionPattern.find(line) ?: return null
        val userVersion = matchResult.groupValues[1]

        // Check if user version is supported
        if (userVersion in supportedVersions) return null

        // Find the nearest suggested version. In this case, the next highest supported version.
        // You can also adjust this logic to provide more intelligent suggestions.
        val suggestedVersion = supportedVersions.find { it > userVersion }

        return if (suggestedVersion != null) {
            "You're running version $userVersion which isn't supported. Consider upgrading to version $suggestedVersion."
        } else {
            "You're running version $userVersion which isn't supported. Consider downgrading to a supported version."
        }
    }
}
