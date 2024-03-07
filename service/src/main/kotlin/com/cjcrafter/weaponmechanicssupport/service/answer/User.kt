package com.cjcrafter.weaponmechanicssupport.service.answer

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains information for a user asking a question. This is used to uniquely
 * identify who is asking, and what they have access to.
 *
 * @property name The nickname of the user.
 * @property resources All resources the user has purchased.
 */
data class User(
    @JsonProperty(required = true) val name: String,
    @JsonProperty(required = true) val resources: List<String>,
)