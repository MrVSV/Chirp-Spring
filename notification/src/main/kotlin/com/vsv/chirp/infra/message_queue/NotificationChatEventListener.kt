package com.vsv.chirp.infra.message_queue

import com.vsv.chirp.domain.events.chat.ChatEvent
import com.vsv.chirp.service.PushNotificationService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationChatEventListener(private val pushNotificationService: PushNotificationService) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_CHAT_EVENTS])
    @Transactional
    fun handleChatEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.NewMessage -> {
                println("User created")
                pushNotificationService.sendNewMessageNotifications(
                    recipientUserIds = event.recipientIds.toList(),
                    senderUserId = event.senderId,
                    senderUsername = event.senderUserName,
                    message = event.message,
                    chatId = event.chatId
                )
            }
        }
    }
}