package com.vsv.chirp.domain.exception

import com.vsv.chirp.domain.type.ChatMessageId

class MessageNotFoundException(messageId: ChatMessageId): RuntimeException(
    "Message $messageId does not exist in chat"
)