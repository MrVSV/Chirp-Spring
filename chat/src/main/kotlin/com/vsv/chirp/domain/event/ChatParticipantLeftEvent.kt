package com.vsv.chirp.domain.event

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId,
)
