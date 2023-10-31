package com.cjcrafter.weaponmechanicssupport.log

class ProtocolLibErrorScraper : LogScraper {
    override fun scrape(line: String): String? {
        if (!line.contains("[ProtocolLib] INTERNAL ERROR: Cannot load ProtocolLib."))
            return null

        return "ProtocolLib failed to load. Try using the latest dev build: https://ci.dmulloy2.net/job/ProtocolLib/lastBuild/artifact/build/libs/ProtocolLib.jar "
    }
}
