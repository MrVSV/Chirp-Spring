package com.vsv.chirp.api.mappers

import com.vsv.chirp.api.dto.DeviceTokenDto
import com.vsv.chirp.api.dto.PlatformDto
import com.vsv.chirp.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        userId = userId,
        token = token,
        createdAt = createdAt,
    )
}

fun DeviceToken.Platform.toPlatformDto(): PlatformDto {
    return when(this) {
        DeviceToken.Platform.ANDROID -> PlatformDto.ANDROID
        DeviceToken.Platform.IOS -> PlatformDto.IOS
    }
}

fun PlatformDto.toPlatform(): DeviceToken.Platform {
    return when(this) {
        PlatformDto.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformDto.IOS -> DeviceToken.Platform.IOS
    }
}