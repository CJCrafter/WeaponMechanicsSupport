package com.cjcrafter.weaponmechanicssupport.log

interface LogScraper {
    fun scrape(line: String): String?

    companion object {
        fun strip(line: String): String {
            val pattern = """\[\d{1,2}:\d{1,2}:\d{1,2}] \[.*?]:\s*""".toRegex()
            return pattern.replace(line, "")
        }

        fun stripPlugin(line: String): Pair<String?, String> {
            val pattern = """\[(.*?)]\s*""".toRegex()
            val strippedLine = strip(line)
            val (plugin) = pattern.find(strippedLine)?.destructured ?: return null to strippedLine

            return plugin to pattern.replace(strippedLine, "")
        }
    }
}