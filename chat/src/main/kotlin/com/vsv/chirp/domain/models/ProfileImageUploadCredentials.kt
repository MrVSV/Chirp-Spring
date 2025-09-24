package com.vsv.chirp.domain.models

import java.time.Instant

data class ProfileImageUploadCredentials(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>,
    val expiresAt: Instant,
)
