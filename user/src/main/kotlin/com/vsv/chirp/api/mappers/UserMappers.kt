package com.vsv.chirp.api.mappers

import com.vsv.chirp.api.dto.AuthenticatedUserDto
import com.vsv.chirp.api.dto.UserDto
import com.vsv.chirp.domain.model.AuthenticatedUser
import com.vsv.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail
    )
}