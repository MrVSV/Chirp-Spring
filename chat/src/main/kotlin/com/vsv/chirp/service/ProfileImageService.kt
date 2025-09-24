package com.vsv.chirp.service

import com.vsv.chirp.domain.event.ProfileImageUpdatedEvent
import com.vsv.chirp.domain.exception.ChatParticipantNotFoundException
import com.vsv.chirp.domain.exception.InvalidProfileImageException
import com.vsv.chirp.domain.models.ProfileImageUploadCredentials
import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.repositories.ChatParticipantRepository
import com.vsv.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class ProfileImageService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${supabase.url}") val supabaseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String,
    ): ProfileImageUploadCredentials {
        return supabaseStorageService.generateSignedUploadUrl(userId, mimeType)
    }

    @Transactional
    fun deleteProfileImage(userId: UserId) {
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)


        participant.profileImageUrl?.let { url ->
            chatParticipantRepository.save(
                participant.apply { this.profileImageUrl = null }
            )

            supabaseStorageService.deleteFile(url)

            applicationEventPublisher.publishEvent(
                ProfileImageUpdatedEvent(
                    userId = userId,
                    newImageUrl = null
                )
            )
        }

    }

    @Transactional
    fun confirmProfileImageUpload(
        userId: UserId,
        publicUrl: String
    ) {
        if(!publicUrl.startsWith(supabaseUrl)){
            throw InvalidProfileImageException("Invalid profile image URL")
        }

        val participant = chatParticipantRepository.findByIdOrNull(userId)
        ?: throw ChatParticipantNotFoundException(userId)

        val oldImageUrl = participant.profileImageUrl

        chatParticipantRepository.save(
            participant.apply { this.profileImageUrl = publicUrl }
        )

        try {
            oldImageUrl?.let { supabaseStorageService.deleteFile(it) }
        } catch (e: Exception) {
            logger.warn("Unable to delete old image file for $userId", e)
        }

        applicationEventPublisher.publishEvent(
            ProfileImageUpdatedEvent(
                userId = userId,
                newImageUrl = publicUrl
            )
        )
    }
}