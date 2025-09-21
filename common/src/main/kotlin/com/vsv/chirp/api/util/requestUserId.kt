package com.vsv.chirp.api.util

import com.vsv.chirp.domain.exception.UnauthorizedException
import com.vsv.chirp.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()