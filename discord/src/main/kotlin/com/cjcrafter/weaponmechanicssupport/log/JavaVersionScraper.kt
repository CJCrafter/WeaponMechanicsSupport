package com.cjcrafter.weaponmechanicssupport.log

class JavaVersionScraper : LogScraper {

    private var errorPluginName: String? = null

    override fun scrape(line: String): String? {
        // Detects a loading error for a plugin we care about
        if (line.contains("Could not load") && line.contains("MechanicsCore")) {
            errorPluginName = line.split("'")[1]
            return null
        }

        // If the errorPluginName was set, and the current line contains the UnsupportedClassVersionError
        if (errorPluginName != null && line.contains("java.lang.UnsupportedClassVersionError")) {
            val detailedError = "Java version mismatch detected for $errorPluginName. Ensure that the server and this plugin are compatible in terms of their required Java versions."
            // Reset the state for next use
            errorPluginName = null
            return detailedError
        }

        errorPluginName = null
        return null
    }
}
