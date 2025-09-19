package com.vsv.chirp.service

import com.vsv.chirp.domain.exception.EmailNotVerifiedException
import com.vsv.chirp.domain.exception.InvalidCredentialsException
import com.vsv.chirp.domain.exception.InvalidTokenException
import com.vsv.chirp.domain.exception.UserAlreadyExistsException
import com.vsv.chirp.domain.exception.UserNotFoundException
import com.vsv.chirp.domain.model.AuthenticatedUser
import com.vsv.chirp.domain.model.User
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.RefreshTokenEntity
import com.vsv.chirp.infra.database.entities.UserEntity
import com.vsv.chirp.infra.database.repositories.RefreshTokenRepository
import com.vsv.chirp.infra.database.repositories.UserRepository
import com.vsv.chirp.infra.mappers.toUser
import com.vsv.chirp.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val emailVerificationService: EmailVerificationService
) {

    @Transactional
    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim(),
        )
        if (user != null) throw UserAlreadyExistsException()
        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password)
            )
        ).toUser()
        val token = emailVerificationService.createVerificationToken(email.trim())
        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if(!user.hasVerifiedEmail) throw EmailNotVerifiedException()

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId, refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if(!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }
        val userId = jwtService.getUserIdFromJWT(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val hashed = hashToken(refreshToken)

        return user.id?.let {userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            ) ?: throw InvalidTokenException("Invalid refresh token")
            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )
            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId, refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()

    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)
        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed,
                createdAt = Instant.now()
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)

    }
}