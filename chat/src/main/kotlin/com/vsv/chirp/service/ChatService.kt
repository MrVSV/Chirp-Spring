package com.vsv.chirp.service

import com.vsv.chirp.api.dto.ChatMessageDto
import com.vsv.chirp.api.mappers.toChatMessageDto
import com.vsv.chirp.domain.event.ChatCreatedEvent
import com.vsv.chirp.domain.event.ChatParticipantsJoinedEvent
import com.vsv.chirp.domain.event.ChatParticipantLeftEvent
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
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
    ): Chat? {
        return chatRepository.findChatById(chatId, requestUserId)
            ?.toChat(lastMessageForChat(chatId)
        )
    }

    fun findChatsByUserId(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatId(chatIds.toSet())
            .associateBy {it.chatId}

        return chatEntities
            .map { it.toChat(lastMessage = latestMessages[it.id]?.toChatMessage()) }
            .sortedByDescending { it.lastActivityAt }
    }

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

        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants,
            )
        ).toChat(null).also { entity ->
            applicationEventPublisher.publishEvent(
                ChatCreatedEvent(
                    chatId = entity.id,
                    participantIds = entity.participants.map { it.userId }
                )
            )

        }
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

        val updatedChat = chatRepository.save(
            chat.apply {
                participants = chat.participants + users
            }
        ).toChat(lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = otherUserIds,
            )
        )
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

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId,
            )
        )
    }

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int,
    ): List<ChatMessageDto> {
        return chatMessageRepository.findByChatIdBefore(
            chatId = chatId,
            before = before ?: Instant.now(),
            pageable = PageRequest.of(0, pageSize)
        )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatId(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}