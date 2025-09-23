package com.vsv.chirp.domain.event

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)
