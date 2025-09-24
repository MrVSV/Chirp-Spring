package com.vsv.chirp.domain.exception

class InvalidProfileImageException(
    override val message: String? = null,
): RuntimeException(message?: "invalid profile image data")
