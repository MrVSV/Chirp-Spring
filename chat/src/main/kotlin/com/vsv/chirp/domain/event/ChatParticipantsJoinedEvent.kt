package com.vsv.chirp.domain.event

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>,
)
