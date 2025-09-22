package com.vsv.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.vsv.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantsToChatRequest @JsonCreator constructor(
    @field:Size(min = 1)
    @JsonProperty("userIds")
    val userIds: List<UserId>,
)
