package com.terminatorssh.terminator.data.local.dao

import androidx.room.*
import com.terminatorssh.terminator.data.local.model.BlobEntity

@Dao
interface BlobDao {
    @Query("SELECT * FROM encrypted_blobs WHERE is_deleted = 0")
    suspend fun getAllActiveBlobs(): List<BlobEntity>

    @Query("SELECT * FROM encrypted_blobs WHERE updated_at > :sinceTimestamp")
    suspend fun getBlobsModifiedSince(sinceTimestamp: String): List<BlobEntity>

    @Query("SELECT * FROM encrypted_blobs")
    suspend fun getAllBlobsIncludeDeleted(): List<BlobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlobs(blobs: List<BlobEntity>)

    @Query("DELETE FROM encrypted_blobs")
    suspend fun nukeTable()
}