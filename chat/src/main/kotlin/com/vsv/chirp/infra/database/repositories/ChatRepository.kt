package com.vsv.chirp.infra.database.repositories

import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository: JpaRepository<ChatEntity, ChatId> {
    @Query("""
        SELECT c 
        FROM ChatEntity c 
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE c.id = :chatId 
        AND EXISTS (
            SELECT 1 
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """)
    fun findChatById(chatId: ChatId, userId: UserId): ChatEntity?

    @Query("""
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """)
    fun findAllByUserId(userId: UserId): List<ChatEntity>
}