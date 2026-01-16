package com.terminatorssh.terminator.data.mapper

import com.terminatorssh.terminator.data.local.model.BlobEntity
import com.terminatorssh.terminator.data.remote.dto.EncryptedBlobDto

class SyncMapper {

    fun toDto(entity: BlobEntity): EncryptedBlobDto {
        return EncryptedBlobDto(
            id = entity.id,
            blob = entity.blob,
            iv = entity.iv,
            updatedAt = entity.updated_at,
            isDeleted = entity.is_deleted
        )
    }

    fun toEntity(dto: EncryptedBlobDto): BlobEntity {
        return BlobEntity(
            id = dto.id,
            blob = dto.blob,
            iv = dto.iv,
            updated_at = dto.updatedAt,
            is_deleted = dto.isDeleted
        )
    }
}