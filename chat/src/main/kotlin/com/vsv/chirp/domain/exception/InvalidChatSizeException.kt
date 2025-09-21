package com.vsv.chirp.domain.exception

import java.lang.RuntimeException

class InvalidChatSizeException: RuntimeException(
    "There must be at least 2 participants to create a chat."
)