package com.vsv.chirp.api.controllers

import com.vsv.chirp.api.dto.ChatParticipantDto
import com.vsv.chirp.api.mappers.toChatParticipantDto
import com.vsv.chirp.api.util.requestUserId
import com.vsv.chirp.service.ChatParticipantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/chat/participants")
class ChatParticipantController(private val chatParticipantService: ChatParticipantService) {

    @GetMapping
    fun getChatParticipantByEmailOrUsername(
        @RequestParam(required = false) query: String?,
    ): ChatParticipantDto {
        val participant = if (query == null){
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }
        return participant?.toChatParticipantDto()
            ?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}