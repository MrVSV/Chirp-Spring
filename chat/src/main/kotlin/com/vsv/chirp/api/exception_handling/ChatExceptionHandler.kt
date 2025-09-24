package com.vsv.chirp.api.exception_handling

import com.vsv.chirp.domain.exception.ChatNotFoundException
import com.vsv.chirp.domain.exception.ChatParticipantNotFoundException
import com.vsv.chirp.domain.exception.InvalidChatSizeException
import com.vsv.chirp.domain.exception.InvalidProfileImageException
import com.vsv.chirp.domain.exception.MessageNotFoundException
import com.vsv.chirp.domain.exception.StorageException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        ChatParticipantNotFoundException::class,
        MessageNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(
        e: Exception
    ) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onUserAlreadyExists(
        e: InvalidChatSizeException
    ) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )

    @ExceptionHandler(InvalidProfileImageException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfileImage(
        e: InvalidProfileImageException
    ) = mapOf(
        "code" to "INVALID_PROFILE_IMAGE",
        "message" to e.message
    )

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorageException (
        e: StorageException
    ) = mapOf(
        "code" to "STORAGE_ERROR",
        "message" to e.message
    )
}