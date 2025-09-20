package com.vsv.chirp.domain.models

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant,
)
