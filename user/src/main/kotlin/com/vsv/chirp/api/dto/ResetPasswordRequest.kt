package com.vsv.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.vsv.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("token")
    val token: String,
    @field:Password
    @JsonProperty("newPassword")
    val newPassword: String
)
