package com.vsv.chirp.api.dto.ws

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.ChatMessageId

data class DeleteMessageDto(
    val chatId: ChatId,
    val messageId: ChatMessageId
)
