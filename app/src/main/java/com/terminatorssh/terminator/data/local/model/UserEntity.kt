package com.terminatorssh.terminator.data.local.model

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val key_salt: String,
    val auth_salt: String,
    val encrypted_master_key: String,
    val login_hash: String,
    val server_url: String?,
    val last_sync_time: String? // null = need initial sync
)