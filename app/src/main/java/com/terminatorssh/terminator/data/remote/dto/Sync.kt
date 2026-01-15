package com.terminatorssh.terminator.data.remote.dto

data class EncryptedBlobDto(
    val id: String,
    val blob: String,
    val iv: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class SyncRequest(
    val blobs: List<EncryptedBlobDto>,
    val lastSyncTime: String,
    val userId: String)

data class SyncResponse(
    val blobs: List<EncryptedBlobDto>,
    val syncTime: String)