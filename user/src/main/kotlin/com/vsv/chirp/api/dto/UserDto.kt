package com.vsv.chirp.api.dto

import com.vsv.chirp.domain.model.UserId

data class UserDto(
    val id: UserId,
    val username: String,
    val email: String,
    val hasVerifiedEmail: Boolean,
)
