package com.vsv.chirp.api.dto.ws

import com.vsv.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)
