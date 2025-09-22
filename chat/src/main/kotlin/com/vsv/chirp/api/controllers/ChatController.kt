package com.vsv.chirp.api.controllers

import com.vsv.chirp.api.dto.AddParticipantsToChatRequest
import com.vsv.chirp.api.dto.ChatDto
import com.vsv.chirp.api.dto.CreateChatRequest
import com.vsv.chirp.api.mappers.toChatDto
import com.vsv.chirp.api.util.requestUserId
import com.vsv.chirp.domain.type.ChatId
import com.vsv.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

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