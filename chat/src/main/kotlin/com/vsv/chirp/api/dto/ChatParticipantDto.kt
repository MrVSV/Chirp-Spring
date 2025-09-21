package com.vsv.chirp.api.dto

import com.vsv.chirp.domain.type.UserId

data class ChatParticipantDto(
    val id: UserId,
    val username: String,
    val email: String,
    val profileImageUrl: String?,

)
