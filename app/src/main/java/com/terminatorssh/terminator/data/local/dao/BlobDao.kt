package com.terminatorssh.terminator.data.local.dao

import androidx.room.*
import com.terminatorssh.terminator.data.local.model.BlobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlobDao {
    @Query("SELECT * FROM encrypted_blobs WHERE is_deleted = 0")
    fun getAllActiveBlobsFlow(): Flow<List<BlobEntity>>

    @Query("SELECT * FROM encrypted_blobs WHERE updated_at > :sinceTimestamp")
    suspend fun getBlobsModifiedSince(sinceTimestamp: String): List<BlobEntity>

    @Query("SELECT * FROM encrypted_blobs")
    suspend fun getAllBlobsIncludeDeleted(): List<BlobEntity>

    @Query("SELECT * FROM encrypted_blobs WHERE id = :id")
    suspend fun getBlobById(id: String): BlobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlobs(blobs: List<BlobEntity>)

    @Query("DELETE FROM encrypted_blobs")
    suspend fun nukeTable()
}