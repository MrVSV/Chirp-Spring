package com.vsv.chirp.domain.exception

import com.vsv.chirp.domain.type.UserId

class ChatParticipantNotFoundException(userId: UserId): RuntimeException(
    "The chat participant with ID $userId was not found."
)
