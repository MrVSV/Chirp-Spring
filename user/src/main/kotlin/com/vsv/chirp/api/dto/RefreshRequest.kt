package com.vsv.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshRequest @JsonCreator constructor(
    @JsonProperty("refreshToken")
    val refreshToken: String
)