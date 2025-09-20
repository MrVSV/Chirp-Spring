package com.vsv.chirp.domain.events.user

import com.vsv.chirp.domain.events.ChirpEvent
import com.vsv.chirp.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Instant = Instant.now(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE
): ChirpEvent {

    data class Created(
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_CREATED_KEY
    ): UserEvent()

    data class Verified(
        val userId: UserId,
        val email: String,
        val username: String,
        override val eventKey: String = UserEventConstants.USER_VERIFIED
    ): UserEvent()

    data class RequestResendVerification(
        val userId: UserId,
        val email: String,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESEND_VERIFICATION
    ): UserEvent()

    data class RequestResetPassword(
        val userId: UserId,
        val email: String,
        val username: String,
        val resetPasswordToken: String,
        val expiresInMinutes: Long,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESET_PASSWORD
    ): UserEvent()
}