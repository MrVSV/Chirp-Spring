package com.vsv.chirp.service

import com.vsv.chirp.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class MessageCacheEvictionHelper {

    @CacheEvict(
        cacheNames = ["messages"],
        key = "#chatId",
    )
    fun evictMessageCache(chatId: ChatId) {

    }
}