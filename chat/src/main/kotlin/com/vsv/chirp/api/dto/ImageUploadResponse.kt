package com.vsv.chirp.api.dto

import java.time.Instant

data class ImageUploadResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>,
    val expiresAt: Instant,
)
