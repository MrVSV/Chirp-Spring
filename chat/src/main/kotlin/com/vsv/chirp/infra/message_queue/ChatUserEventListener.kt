package com.vsv.chirp.infra.message_queue

import com.vsv.chirp.domain.events.user.UserEvent
import com.vsv.chirp.domain.models.ChatParticipant
import com.vsv.chirp.service.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        println(event)
        when (event) {
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profileImageUrl = null
                    )
                )
            }
            else -> Unit
        }
    }
}