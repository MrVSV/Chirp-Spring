package com.vsv.chirp.domain.events.chat

import com.vsv.chirp.domain.events.ChirpEvent
import java.time.Instant
import java.util.*

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Instant = Instant.now(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE
): ChirpEvent {

}