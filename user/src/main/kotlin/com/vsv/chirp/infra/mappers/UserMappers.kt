package com.vsv.chirp.infra.mappers

import com.vsv.chirp.domain.model.User
import com.vsv.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = hasVerifiedEmail
    )
}