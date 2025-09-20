package com.vsv.chirp.service

import com.vsv.chirp.domain.events.user.UserEvent
import com.vsv.chirp.domain.exception.EmailNotVerifiedException
import com.vsv.chirp.domain.exception.InvalidCredentialsException
import com.vsv.chirp.domain.exception.InvalidTokenException
import com.vsv.chirp.domain.exception.SamePasswordException
import com.vsv.chirp.domain.exception.UserNotFoundException
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.PasswordResetTokenEntity
import com.vsv.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.vsv.chirp.infra.database.repositories.RefreshTokenRepository
import com.vsv.chirp.infra.database.repositories.UserRepository
import com.vsv.chirp.infra.message_queue.EventPublisher
import com.vsv.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val eventPublisher: EventPublisher,
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return
        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }
        passwordResetTokenRepository.invalidateActiveTokensForUser(user)
        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
        )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = email,
                username = user.username,
                resetPasswordToken = token.token,
                expiresInMinutes = expiryMinutes
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")
        if (resetToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }
        if (resetToken.isExpired) {
            throw InvalidTokenException("Email verification token is already expired")
        }

        val user = resetToken.user
        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }
        val newHashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword
            }
        )
        passwordResetTokenRepository.save(
            resetToken.apply { this.usedAt = Instant.now() }
        )
        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()
        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }
        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }
        refreshTokenRepository.deleteByUserId(userId)
        val newHashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply { this.hashedPassword = newHashedPassword }
        )

    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}