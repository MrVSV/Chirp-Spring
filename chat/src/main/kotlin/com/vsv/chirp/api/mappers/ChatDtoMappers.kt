package com.vsv.chirp.api.mappers

import com.vsv.chirp.api.dto.ChatDto
import com.vsv.chirp.api.dto.ChatMessageDto
import com.vsv.chirp.api.dto.ChatParticipantDto
import com.vsv.chirp.domain.models.Chat
import com.vsv.chirp.domain.models.ChatMessage
import com.vsv.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map { it.toChatParticipantDto() },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        senderId = sender.userId,
        createdAt = createdAt,
        content = content
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        id = userId,
        username = username,
        email = email,
        profileImageUrl = profileImageUrl
    )
}