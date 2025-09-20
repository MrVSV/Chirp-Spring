package com.vsv.chirp.infra.message_queue

import com.vsv.chirp.domain.events.user.UserEvent
import com.vsv.chirp.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class NotificationUserEventListener(private val emailService: EmailService) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                println("User created")
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }
            is UserEvent.RequestResendVerification -> {
                println("Request resend verification")
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }
            is UserEvent.RequestResetPassword -> {
                println("Request reset password")
                emailService.sendPasswordResetEmail(
                    email = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.resetPasswordToken,
                    expiresIn = Duration.ofMinutes(event.expiresInMinutes)
                )
            }
            else -> Unit
        }
    }
}