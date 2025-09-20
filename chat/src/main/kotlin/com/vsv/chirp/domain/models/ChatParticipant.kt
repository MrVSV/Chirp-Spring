package com.vsv.chirp.domain.models

import com.vsv.chirp.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profileImageUrl: String?,
)
