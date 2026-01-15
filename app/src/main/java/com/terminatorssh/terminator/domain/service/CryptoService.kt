package com.terminatorssh.terminator.domain.service

import com.terminatorssh.terminator.domain.model.EncryptedData

interface CryptoService {
    suspend fun deriveKEK(
        password: String,
        keySaltBase64: String): ByteArray

    fun encryptAES(
        data: ByteArray,
        key: ByteArray): EncryptedData

    fun decryptAES(
        data: EncryptedData,
        key: ByteArray): ByteArray

    fun unpackBlob(
        packedBlobBase64: String): EncryptedData

    fun sha256(inputBase64: String): String
}