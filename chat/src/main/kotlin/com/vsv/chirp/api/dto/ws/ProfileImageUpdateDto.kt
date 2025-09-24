package com.vsv.chirp.api.dto.ws

import com.vsv.chirp.domain.type.UserId

data class ProfileImageUpdateDto(
    val userId: UserId,
    val newImageUrl: String?
)
