package com.vsv.chirp.service

import com.vsv.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.vsv.chirp.infra.database.repositories.UserRepository
import com.vsv.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}") private val expiryMinutes: Long,

) {

}