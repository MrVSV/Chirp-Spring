package com.vsv.chirp.domain.exception

import java.lang.RuntimeException

class RateLimitException(
    val resetsInSeconds: Long,
): RuntimeException(
    "Rate limit exceeded. Please try again in $resetsInSeconds seconds."
)