package com.vsv.chirp.infra.database.repositories

import com.vsv.chirp.domain.type.UserId
import com.vsv.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, UserId> {

    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?

}