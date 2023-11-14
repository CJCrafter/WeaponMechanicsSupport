package com.cjcrafter.weaponmechanicssupport.openai

import com.cjcrafter.weaponmechanicssupport.Main
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URISyntaxException
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess

fun outputDir(name: String): File {
    val codeSourceLocation = try {
        Main::class.java.protectionDomain.codeSource.location
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        exitProcess(1)
    }

    val jarFile = File(codeSourceLocation.toURI())

    // Determine if running from IDE or jar
    val isRunningFromJar = jarFile.path.endsWith(".jar")

    val parentDir = if (isRunningFromJar) {
        // Running from jar
        jarFile.parentFile
    } else {
        // Running from IDE
        File(System.getProperty("user.dir"))
    }

    val workDir = File(parentDir, "work")
    val outputDir = File(workDir, name)

    if (!outputDir.exists()) {
        if (outputDir.mkdirs()) {
            println("Created directory: ${outputDir.path}")
        } else {
            println("Failed to create directory: ${outputDir.path}")
        }
    }

    return outputDir
}

fun downloadAndExtract(zipUrl: String, extractTo: String) {
    val url = URL(zipUrl)
    ZipInputStream(url.openStream()).use { zip ->
        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
            val filePath = extractTo + File.separator + entry.name
            if (!entry.isDirectory) {
                // If it's a file, extract it
                extractFile(zip, filePath)
            } else {
                // If it's a directory, make the directory
                File(filePath).mkdir()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
}

private fun extractFile(zipIn: ZipInputStream, filePath: String) {
    BufferedOutputStream(FileOutputStream(filePath)).use { bos ->
        val bytesIn = ByteArray(4096)
        var read: Int
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
    }
}

fun mergeFiles(read: File, regex: Regex, includeNames: Boolean = false) {
    val outputFile = File(read.parentFile, "${read.nameWithoutExtension}_merged.txt")
    outputFile.delete()

    read.walkTopDown().forEach { file ->
        if (file.isFile && regex.matches(file.name)) {

            // Print the file path up until the `read` file
            if (includeNames) {
                val comment = """
                    #
                    # This is from the file ${read.name}${file.path.substringAfter(read.path)}
                    #
                """.trimIndent()
                outputFile.appendText("\n")
                outputFile.appendText(comment)
                outputFile.appendText("\n")
            }

            val content = file.readText()
            outputFile.appendText(content)
            outputFile.appendText("\n")
        }
    }
}

fun main() {
    val outputDir = outputDir("weaponmechanics")
    //File(outputDir, "WeaponMechanicsWiki-master").deleteRecursively()

    val zipUrl = "https://github.com/WeaponMechanics/WeaponMechanicsWiki/archive/refs/heads/master.zip"
    //downloadAndExtract(zipUrl, outputDir.path)

    mergeFiles(File(outputDir, "WeaponMechanicsWiki-master"),  Regex("^(?!SUMMARY).+\\.md$"))
    mergeFiles(File(outputDir, "WeaponMechanics"), Regex("^.+\\.yml$"), true)
}