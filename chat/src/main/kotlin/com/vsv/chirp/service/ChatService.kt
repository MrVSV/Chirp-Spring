package com.vsv.chirp.service

import com.vsv.chirp.domain.exception.ChatNotFoundException
import com.vsv.chirp.domain.exception.ChatParticipantNotFoundException
import com.vsv.chirp.domain.exception.ForbiddenException
import com.vsv.chirp.domain.exception.InvalidChatSizeException
import com.vsv.chirp.domain.models.Chat
import com.vsv.chirp.domain.models.ChatMessage
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.ChatEntity
import com.vsv.chirp.infra.database.mappers.toChat
import com.vsv.chirp.infra.database.mappers.toChatMessage
import com.vsv.chirp.infra.database.repositories.ChatMessageRepository
import com.vsv.chirp.infra.database.repositories.ChatParticipantRepository
import com.vsv.chirp.infra.database.repositories.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun creatChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {

        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds,
        )

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants,
            )
        ).toChat(null)
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        otherUserIds: Set<UserId>,
    ): Chat {


        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any { it.userId == requestUserId }
        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }

        val users = otherUserIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        logger.info("Adding participants to chat $chatId")

        val updatedChat = chatRepository.save(
            chat.apply {
                participants = chat.participants + users
            }
        ).toChat(lastMessage)

        return updatedChat
    }

    @Transactional
    fun removeParticipantsFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        logger.info("Find last message for $chatId")
        return chatMessageRepository
            .findLatestMessagesByChatId(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}