package com.vsv.chirp.domain.events.chat

object ChatEventConstants {

    const val CHAT_EXCHANGE = "chat.events"

    const val CHAT_NEW_MESSAGE = "chat.new_message"
    const val USER_VERIFIED = "user.verified"
    const val USER_REQUEST_RESEND_VERIFICATION = "user.request_resend_verification"
    const val USER_REQUEST_RESET_PASSWORD = "user.request_reset_password"
}