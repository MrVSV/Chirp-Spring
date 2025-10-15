package com.vsv.chirp.api.websocket

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.vsv.chirp.api.dto.ws.*
import com.vsv.chirp.api.mappers.toChatMessageDto
import com.vsv.chirp.domain.event.ChatCreatedEvent
import com.vsv.chirp.domain.event.ChatParticipantLeftEvent
import com.vsv.chirp.domain.event.ChatParticipantsJoinedEvent
import com.vsv.chirp.domain.event.MessageDeletedEvent
import com.vsv.chirp.domain.event.ProfileImageUpdatedEvent
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.service.ChatMessageService
import com.vsv.chirp.service.ChatService
import com.vsv.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val chatService: ChatService,
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    companion object {
        private const val PING_INTERVAL_MS = 30_000L
        private const val PONG_TIMEOUT_MS = 60_000L
    }
    private val mapper = objectMapper
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was close due to missing Authorization header")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }
        val userId = jwtService.getUserIdFromJWT(authHeader)

        val userSession = UserSession(
            userId = userId,
            session = session
        )

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                val chatIds = chatService.findChatsByUserId(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply { addAll(chatIds) }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("WebSocket connection established for user $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val websocketMessage = mapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )
            when (websocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = mapper.readValue(
                        websocketMessage.payload,
                        SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId
                    )
                }
            }
        } catch (e: JsonMappingException) {
            logger.warn("Could not parse message ${message.payload}", e)
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connectionLock.write {
            sessions.remove(session.id)?.let { userSession -> //removed session
                val userId = userSession.userId

                userToSessions.compute(userId) { _, sessions ->
                    sessions
                        ?.apply { remove(session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }

                userChatIds[userId]?.forEach { chatId ->
                    chatToSessions.compute(chatId) { _, sessions ->
                        sessions
                            ?.apply { remove(session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }

                }

                logger.info("WebSocket connection closed for user $userId")
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("WebSocket connection error for ${session.id}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = mapper.writeValueAsString(
                    DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantsJoinedEvent) {
        updateChatForUsers(
            chatId = event.chatId,
            userIds = event.userIds.toList()
        )
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = mapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId,
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatCreated(event: ChatCreatedEvent) {
        updateChatForUsers(
            chatId = event.chatId,
            userIds = event.participantIds
        )
    }

    private fun updateChatForUsers(
        chatId: ChatId,
        userIds: List<UserId>,
    ) {
        connectionLock.write {
            userIds.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply {
                        add(chatId)
                    }
                }
                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(chatId) { _, sessions ->
                        (sessions ?: mutableSetOf()).apply {
                            add(sessionId)
                        }
                    }
                }
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProfileImageUpdated(event: ProfileImageUpdatedEvent) {

        val userChats = connectionLock.read {
            userChatIds[event.userId]?.toList() ?: emptyList()
        }

        val dto = ProfileImageUpdateDto(
            userId = event.userId,
            newImageUrl = event.newImageUrl
        )

        val sessionIds = mutableSetOf<String>()
        userChats.forEach { chatId ->
            connectionLock.read {
                chatToSessions[chatId]?.let { sessions ->
                    sessionIds.addAll(sessions)
                }
            }
        }

        val websocketMessage = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.PROFILE_IMAGE_UPDATED,
            payload = mapper.writeValueAsString(dto)
        )

        val messageJson = mapper.writeValueAsString(websocketMessage)

        sessionIds.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach
            try {
                if(userSession.session.isOpen) {
                    userSession.session.sendMessage(TextMessage(messageJson))
                }
            } catch (e: Exception) {
                logger.error("Could not send profile update to session $sessionId", e)
            }
        }

    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    fun pingClient() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()

        val sessionsSnapshot = connectionLock.read {
            sessions.toMap()
        }

        sessionsSnapshot.forEach { (sessionId, userSession) ->
            try {
                if (userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestamp
                    if ((currentTime - lastPong) > PONG_TIMEOUT_MS) {
                        logger.warn("Client $sessionId has timed out, closing connection")
                        sessionsToClose.add(sessionId)
                        return@forEach
                    }
                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Sent ping to {}", userSession.userId)
                }
            } catch (e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(sessionId)
            }
        }

        sessionsToClose.forEach { sessionId ->
            connectionLock.read {
                sessions[sessionId]?.session?.let { session ->
                    try {
                        session.close(CloseStatus.GOING_AWAY.withReason("Ping timeout"))
                    } catch (e: Exception) {
                        logger.error("Could not close session for user ${session.id}", e)
                    }
                }
            }
        }
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        connectionLock.write {
            sessions.compute(session.id) { _, userSession ->
                userSession?.copy(
                    lastPongTimestamp = System.currentTimeMillis()
                )
            }
        }
        logger.debug("Received pong from ${session.id}")
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeaveChat(event: ChatParticipantLeftEvent) {
        connectionLock.write {
            userChatIds.compute(event.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }
            userToSessions[event.userId]?.forEach { sessionId ->
                chatToSessions.compute(event.chatId) { _, sessionIds ->
                    sessionIds
                        ?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = mapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId,
                    )
                )
            )
        )
    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId
    ) {
        val userChatsIds = connectionLock.read {
            userChatIds[senderId]
        } ?: return

        if (dto.chatId !in userChatsIds) {
            return
        }

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = mapper.writeValueAsString(savedMessage.toChatMessageDto())
            )
        )
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        logger.info("broadcast message {}", message)
        val chatSessions = connectionLock.read {
            chatToSessions[chatId]?.toList() ?: emptyList()
        }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message
            )
        }
    }

    private fun sendToUser(
        userId: UserId,
        message: OutgoingWebSocketMessage
    ) {
        val userSessions = connectionLock.read {
            userToSessions[userId] ?: emptySet()
        }

        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = mapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (e: Exception) {
                    logger.error("Error while sending message to $userId", e)
                }
            }
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = mapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = mapper.writeValueAsString(error),
            )
        )

        try {
            session.sendMessage(
                TextMessage(webSocketMessage)
            )
        } catch (e: Exception) {
            logger.warn("Could not send error message", e)
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        val lastPongTimestamp: Long = System.currentTimeMillis(),
    )
}