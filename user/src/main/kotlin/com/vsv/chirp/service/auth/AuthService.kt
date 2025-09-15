package com.vsv.chirp.service.auth

import com.vsv.chirp.domain.exception.UserAlreadyExistsException
import com.vsv.chirp.domain.model.User
import com.vsv.chirp.infra.database.entities.UserEntity
import com.vsv.chirp.infra.database.repositories.UserRepository
import com.vsv.chirp.infra.mappers.toUser
import com.vsv.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(email: String, username: String, password: String): User {
        val user = repository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim(),
        )

        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = repository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password)
            )
        ).toUser()

        return savedUser
    }
}