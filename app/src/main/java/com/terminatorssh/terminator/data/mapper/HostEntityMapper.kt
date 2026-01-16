package com.terminatorssh.terminator.data.mapper

import com.google.gson.Gson
import com.terminatorssh.terminator.data.local.model.BlobEntity
import com.terminatorssh.terminator.domain.model.Host
import com.terminatorssh.terminator.domain.service.CryptoService
import java.time.Instant
import java.time.format.DateTimeFormatter

class HostDataMapper(
    private val gson: Gson,
    private val cryptoService: CryptoService
) {

    fun mapToHost(entity: BlobEntity, masterKey: ByteArray): Host? {
        return try {
            val encryptedData = cryptoService.unpackBlob(entity.blob)
            val jsonBytes = cryptoService.decryptAES(encryptedData, masterKey)
            val jsonString = String(jsonBytes, Charsets.UTF_8)

            val host = gson.fromJson(jsonString, Host::class.java)
            host.copy(id = entity.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun mapToEntity(host: Host, masterKey: ByteArray): BlobEntity {
        val jsonString = gson.toJson(host)
        val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

        val encryptedData = cryptoService.encryptAES(jsonBytes, masterKey)
        val packedBlob = cryptoService.packBlob(encryptedData)

        val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        return BlobEntity(
            id = host.id,
            blob = packedBlob,
            iv = encryptedData.iv,
            updated_at = now,
            is_deleted = false
        )
    }
}