package com.vsv.chirp.infra.mappers

import com.vsv.chirp.domain.model.DeviceToken
import com.vsv.chirp.infra.database.DeviceTokenEntity
import com.vsv.chirp.infra.database.PlatformEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        id = id,
        userId = userId,
        token = token,
        platform = platform.toPlatform(),
        createdAt = createdAt
    )
}

fun DeviceToken.Platform.toPlatformEntity(): PlatformEntity {
    return when(this) {
        DeviceToken.Platform.ANDROID -> PlatformEntity.ANDROID
        DeviceToken.Platform.IOS -> PlatformEntity.IOS
    }
}

fun PlatformEntity.toPlatform(): DeviceToken.Platform {
    return when(this) {
        PlatformEntity.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformEntity.IOS -> DeviceToken.Platform.IOS
    }
}