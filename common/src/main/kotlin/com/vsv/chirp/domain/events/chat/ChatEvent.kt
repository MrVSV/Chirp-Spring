package com.vsv.chirp.domain.events.chat

import com.vsv.chirp.domain.events.ChirpEvent
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId
import java.time.Instant
import java.util.*

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Instant = Instant.now(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE
): ChirpEvent {

    data class NewMessage(
        val senderId: UserId,
        val senderUserName: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ): ChatEvent()
}