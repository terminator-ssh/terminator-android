package com.terminatorssh.terminator.data.local.model

import androidx.room.*

@Entity(tableName = "encrypted_blobs")
data class BlobEntity(
    @PrimaryKey val id: String,
    val blob: String,
    val iv: String,
    val updated_at: String,
    val is_deleted: Boolean
)