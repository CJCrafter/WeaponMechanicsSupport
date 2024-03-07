package com.cjcrafter.weaponmechanicssupport.service

import com.cjcrafter.weaponmechanicssupport.service.answer.Answer
import com.cjcrafter.weaponmechanicssupport.service.answer.AnswerRequest
import com.cjcrafter.weaponmechanicssupport.service.answer.AnswerResponse
import com.cjcrafter.weaponmechanicssupport.service.answer.answerResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class Controller {

    @PostMapping("v1/answer")
    fun answer(@RequestBody request: AnswerRequest): AnswerResponse {
        return answerResponse {
            request(request)
            answers(listOf(Answer("Nope, not happening.")))
        }
    }
}