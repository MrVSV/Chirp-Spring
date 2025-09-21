package com.vsv.chirp.api.controllers

import com.vsv.chirp.api.dto.ChatDto
import com.vsv.chirp.api.dto.CreateChatRequest
import com.vsv.chirp.api.mappers.toChatDto
import com.vsv.chirp.api.util.requestUserId
import com.vsv.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

    @PostMapping("/create-chat")
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.creatChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}