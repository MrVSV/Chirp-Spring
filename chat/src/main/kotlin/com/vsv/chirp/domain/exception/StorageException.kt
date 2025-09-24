package com.vsv.chirp.domain.exception

class StorageException(
    override val message: String? = null,
): RuntimeException(
    message ?: "Storage error"
)