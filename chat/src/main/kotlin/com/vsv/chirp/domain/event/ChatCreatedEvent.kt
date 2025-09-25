package com.vsv.chirp.domain.event

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantIds: List<UserId>
)
