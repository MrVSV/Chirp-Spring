package com.vsv.chirp.domain.model

import com.vsv.chirp.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasVerifiedEmail: Boolean,
)
