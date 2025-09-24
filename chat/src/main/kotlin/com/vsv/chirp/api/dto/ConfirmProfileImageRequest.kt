package com.vsv.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class ConfirmProfileImageRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("publicUrl")
    val publicUrl: String,

)
