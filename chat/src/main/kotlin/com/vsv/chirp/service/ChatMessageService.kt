package com.vsv.chirp.service

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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {



    @Transactional
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


        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender as ChatParticipantEntity,
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if(message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)
    }
}