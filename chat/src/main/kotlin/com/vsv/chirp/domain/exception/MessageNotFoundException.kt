package com.vsv.chirp.domain.exception

import com.vsv.chirp.domain.type.ChatMessageId
import java.lang.RuntimeException

class MessageNotFoundException(messageId: ChatMessageId): RuntimeException(
    "Message $messageId does not exist in chat"
)