package com.vsv.chirp.api.controllers

import com.vsv.chirp.api.dto.AddParticipantsToChatRequest
import com.vsv.chirp.api.dto.ChatDto
import com.vsv.chirp.api.dto.ChatMessageDto
import com.vsv.chirp.api.dto.CreateChatRequest
import com.vsv.chirp.api.mappers.toChatDto
import com.vsv.chirp.api.util.requestUserId
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
) {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }

    @GetMapping("/{chatId}/messages")
    fun getChatMessages(
        @PathVariable("chatId") chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE,
    ) : List<ChatMessageDto> {
        return chatService.getChatMessages(chatId, before, pageSize)
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable("chatId") chatId: ChatId,
    ): ChatDto {
        return chatService.getChatById(chatId, requestUserId)?.toChatDto()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsByUserId(): List<ChatDto> {
        return chatService.findChatsByUserId(requestUserId).map { it.toChatDto() }
    }

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.creatChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addParticipantsToChat(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddParticipantsToChatRequest
    ): ChatDto {
        return chatService.addParticipantsToChat(
            requestUserId = requestUserId,
            chatId = chatId,
            otherUserIds = body.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(
        @PathVariable chatId: ChatId,
    ) {
        return chatService.removeParticipantsFromChat(
            chatId = chatId,
            userId = requestUserId
        )
    }
}