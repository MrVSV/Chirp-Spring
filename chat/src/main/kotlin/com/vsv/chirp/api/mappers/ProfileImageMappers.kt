package com.vsv.chirp.api.mappers

import com.vsv.chirp.api.dto.ImageUploadResponse
import com.vsv.chirp.domain.models.ProfileImageUploadCredentials

fun ProfileImageUploadCredentials.toImageUploadResponse(): ImageUploadResponse {
    return ImageUploadResponse(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers,
        expiresAt = expiresAt
    )
}