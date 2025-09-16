package com.vsv.chirp.infra.database.entities

import com.vsv.chirp.infra.security.TokenGenerator
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service",
    indexes = []
)
class EmailVerificationTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column
    var usedAt: Instant? = null,
    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
) {
    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = Instant.now() > expiresAt
}