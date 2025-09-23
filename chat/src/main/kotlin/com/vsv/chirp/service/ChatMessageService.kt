package com.vsv.chirp.service

import com.vsv.chirp.domain.event.MessageDeletedEvent
import com.vsv.chirp.domain.events.chat.ChatEvent
import com.vsv.chirp.domain.exception.ChatNotFoundException
import com.vsv.chirp.domain.exception.ChatParticipantNotFoundException
import com.vsv.chirp.domain.exception.ForbiddenException
import com.vsv.chirp.domain.exception.MessageNotFoundException
import com.vsv.chirp.domain.models.ChatMessage
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.ChatMessageId
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.ChatMessageEntity
import com.vsv.chirp.infra.database.entities.ChatParticipantEntity
import com.vsv.chirp.infra.database.mappers.toChatMessage
import com.vsv.chirp.infra.database.repositories.ChatMessageRepository
import com.vsv.chirp.infra.database.repositories.ChatParticipantRepository
import com.vsv.chirp.infra.database.repositories.ChatRepository
import com.vsv.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    @CacheEvict(
        cacheNames = ["messages"],
        key = "#chatId",
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null,
    ): ChatMessage {

        val chat = chatRepository.findChatById(chatId, senderId)
            ?:throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: ChatParticipantNotFoundException(senderId)


        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender as ChatParticipantEntity,
            )
        )

        eventPublisher.publish(
            ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUserName = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional

    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ){
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if(message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId,
            )
        )
        evictMessageCache(message.chatId)
    }

    @CacheEvict(
        cacheNames = ["messages"],
        key = "#chatId",
    )
    fun evictMessageCache(chatId: ChatId) {

    }
}