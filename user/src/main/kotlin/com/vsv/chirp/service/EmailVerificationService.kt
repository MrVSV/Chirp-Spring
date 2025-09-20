package com.vsv.chirp.service

import com.vsv.chirp.domain.events.user.UserEvent
import com.vsv.chirp.domain.exception.InvalidTokenException
import com.vsv.chirp.domain.exception.UserNotFoundException
import com.vsv.chirp.domain.model.EmailVerificationToken
import com.vsv.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.vsv.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.vsv.chirp.infra.database.repositories.UserRepository
import com.vsv.chirp.infra.mappers.toEmailVerificationToken
import com.vsv.chirp.infra.mappers.toUser
import com.vsv.chirp.infra.message_queue.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher,
) {

    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email)
        if (token.user.hasVerifiedEmail) {
            return
        }
        eventPublisher.publish(
            UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)
        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )
        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid")
        if(verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }
        if(verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token is already expired")
        }
        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )
        userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        ).toUser()
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}