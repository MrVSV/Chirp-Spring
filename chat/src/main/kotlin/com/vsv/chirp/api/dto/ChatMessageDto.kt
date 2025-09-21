package com.vsv.chirp.api.dto

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.ChatMessageId
import com.vsv.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val senderId: UserId,
    val createdAt: Instant,
    val content: String,
)
