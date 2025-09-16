package com.vsv.chirp.infra.mappers

import com.vsv.chirp.domain.model.EmailVerificationToken
import com.vsv.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken() : EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}