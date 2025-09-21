package com.vsv.chirp.service

import com.vsv.chirp.domain.models.ChatParticipant
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.mappers.toChatParticipant
import com.vsv.chirp.infra.database.mappers.toChatParticipantEntity
import com.vsv.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(chatParticipant: ChatParticipant) {
        chatParticipantRepository.save(chatParticipant.toChatParticipantEntity())
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(normalizedQuery)?.toChatParticipant()
    }


}