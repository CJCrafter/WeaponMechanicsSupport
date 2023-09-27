package com.cjcrafter.weaponmechanicssupport.log

class ProtocolLibUpdateScraper : LogScraper {

    override fun scrape(line: String): String? {
        // Pattern to match the ProtocolLib update message
        val pattern = Regex("""\[ProtocolLib] The updater found an update: (.+) \(Running (.+)\). Download at (.+)""")

        val matchResult = pattern.find(line)
        if (matchResult != null) {
            val (availableVersion, currentVersion, downloadLink) = matchResult.destructured
            return getErrorMessage(currentVersion, availableVersion, downloadLink)
        }

        return null
    }

    companion object {
        @JvmStatic
        fun getErrorMessage(old: String, new: String, download: String): String {
            return "You are running an outdated version of ProtocolLib. You're running $old, but version $new is available. Download the update at $download. If you are running an older version of Minecraft (1.16.5 or older), you can use 4.7.0 instead."
        }
    }
}
