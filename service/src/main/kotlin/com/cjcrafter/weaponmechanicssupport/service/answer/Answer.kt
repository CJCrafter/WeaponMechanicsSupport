package com.cjcrafter.weaponmechanicssupport.service.answer

data class Answer(
    val answer: String,
    val sources: List<String> = listOf(),
)