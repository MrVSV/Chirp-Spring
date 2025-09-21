package com.vsv.chirp.infra.database.entities

import com.vsv.chirp.domain.type.UserId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_participants",
    schema = "chat_service",
    indexes = [
        Index(name = "idx_chat_participant_email", columnList = "email"),
        Index(name = "idx_chat_participant_username", columnList = "username")
    ]
)
class ChatParticipantEntity(
    @Id
    var userId: UserId,
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = true, unique = false)
    var profileImageUrl: String? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)
