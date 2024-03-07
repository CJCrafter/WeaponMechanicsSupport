package com.cjcrafter.autosupport

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AutosupportApplication

fun main(args: Array<String>) {
	runApplication<AutosupportApplication>(*args)
}
