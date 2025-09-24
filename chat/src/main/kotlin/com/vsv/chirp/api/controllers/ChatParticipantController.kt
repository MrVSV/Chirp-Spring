package com.vsv.chirp.api.controllers

import com.vsv.chirp.api.dto.ChatParticipantDto
import com.vsv.chirp.api.dto.ConfirmProfileImageRequest
import com.vsv.chirp.api.dto.ImageUploadResponse
import com.vsv.chirp.api.mappers.toChatParticipantDto
import com.vsv.chirp.api.mappers.toImageUploadResponse
import com.vsv.chirp.api.util.requestUserId
import com.vsv.chirp.service.ChatParticipantService
import com.vsv.chirp.service.ProfileImageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profileImageService: ProfileImageService
) {

    @GetMapping
    fun getChatParticipantByEmailOrUsername(
        @RequestParam(required = false) query: String?,
    ): ChatParticipantDto {
        val participant = if (query == null) {
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }
        return participant?.toChatParticipantDto()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("profile-image-upload")
    fun getProfileImageUrl(
        @RequestParam mimeType: String,
    ): ImageUploadResponse {
        return profileImageService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toImageUploadResponse()
    }

    @PostMapping("confirm-profile-image")
    fun confirmProfileImageUpload(
        @Valid @RequestBody body: ConfirmProfileImageRequest,
    ) {
        profileImageService.confirmProfileImageUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("profile-image")
    fun deleteProfileImage() {
        profileImageService.deleteProfileImage(requestUserId)
    }

}