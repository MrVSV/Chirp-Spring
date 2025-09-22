package com.vsv.chirp.infra.database.mappers

import com.vsv.chirp.domain.models.Chat
import com.vsv.chirp.domain.models.ChatMessage
import com.vsv.chirp.domain.models.ChatParticipant
import com.vsv.chirp.infra.database.entities.ChatEntity
import com.vsv.chirp.infra.database.entities.ChatMessageEntity
import com.vsv.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toChatParticipant()
        }.toSet(),
        lastMessage = lastMessage,
        creator = creator.toChatParticipant(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profileImageUrl = profileImageUrl
    )
}

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profileImageUrl = profileImageUrl
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id!!,
        chatId = chatId,
        sender = sender.toChatParticipant(),
        content = content,
        createdAt = createdAt
    )
}