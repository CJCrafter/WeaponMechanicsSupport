package com.cjcrafter.weaponmechanicssupport.service.answer

data class AnswerResponse(
    val request: AnswerRequest,
    val count: Int,
    val answers: List<Answer>,
) {
    class Builder internal constructor() {
        private var request: AnswerRequest? = null
        private var answers: List<Answer>? = null

        fun request(request: AnswerRequest) = apply { this.request = request }
        fun answers(answers: List<Answer>) = apply { this.answers = answers }
        fun build() = AnswerResponse(request!!, answers!!.size, answers!!)
    }

    companion object {
        fun builder() = Builder()
    }
}

/**
 * Creates an [AnswerResponse] with the provided values.
 *
 * @param block The builder block to set the values.
 * @return The created [AnswerResponse].
 */
fun answerResponse(block: AnswerResponse.Builder.() -> Unit): AnswerResponse {
    return AnswerResponse.builder().apply(block).build()
}