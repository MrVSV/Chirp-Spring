package com.vsv.chirp.domain.event

import com.vsv.chirp.domain.type.UserId

data class ProfileImageUpdatedEvent(
    val userId: UserId,
    val newImageUrl: String?
)
