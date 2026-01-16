package com.terminatorssh.terminator.domain.service

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.terminatorssh.terminator.domain.common.Base64Helper.decodeBase64
import com.terminatorssh.terminator.domain.common.Base64Helper.encodeBase64
import com.terminatorssh.terminator.domain.common.CryptoConstants
import com.terminatorssh.terminator.domain.model.EncryptedData
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ArgonCryptoService : CryptoService {

    private val argon2 = Argon2Kt()

    override suspend fun deriveKEK(password: String, keySaltBase64: String): ByteArray {
        val salt = decodeBase64(keySaltBase64)

        return argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = salt,
            tCostInIterations = CryptoConstants.ITERATIONS,
            mCostInKibibyte = CryptoConstants.MEMORY_COST,
            parallelism = CryptoConstants.PARALLELISM,
            hashLengthInBytes = CryptoConstants.HASH_LENGTH
        ).rawHashAsByteArray()
    }

    override fun encryptAES(data: ByteArray, key: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION)
        val iv = ByteArray(CryptoConstants.GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val cipherTextWithTag = cipher.doFinal(data)

        val tagLength = CryptoConstants.GCM_TAG_LENGTH / 8
        val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - tagLength)
        val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - tagLength, cipherTextWithTag.size)

        return EncryptedData(
            encodeBase64(cipherText),
            encodeBase64(iv),
            encodeBase64(tag)
        )
    }

    override fun decryptAES(
        data: EncryptedData,
        key: ByteArray): ByteArray {
        val cipherText = decodeBase64(data.cipherText)
        val iv = decodeBase64(data.iv)
        val tag = decodeBase64(data.tag)

        val combined = cipherText + tag

        val cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION)
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
        return cipher.doFinal(combined)
    }

    override fun unpackBlob(packedBlobBase64: String): EncryptedData {
        val packedBytes = decodeBase64(packedBlobBase64)

        // IV (12) + Ciphertext (N) + Tag (16)
        val ivLength = CryptoConstants.GCM_IV_LENGTH
        val tagLength = CryptoConstants.GCM_TAG_LENGTH / 8

        if (packedBytes.size < ivLength + tagLength) {
            throw IllegalArgumentException("Blob is too short")
        }

        val iv = packedBytes.copyOfRange(0, ivLength)
        val tag = packedBytes.copyOfRange(packedBytes.size - tagLength, packedBytes.size)
        val cipherText = packedBytes.copyOfRange(ivLength, packedBytes.size - tagLength)

        return EncryptedData(
            cipherText = encodeBase64(cipherText),
            iv = encodeBase64(iv),
            tag = encodeBase64(tag)
        )
    }

    override fun packBlob(blob: EncryptedData): String {
        val ivBytes = decodeBase64(blob.iv)
        val cipherBytes = decodeBase64(blob.cipherText)
        val tagBytes = decodeBase64(blob.tag)

        val combined = ivBytes + cipherBytes + tagBytes
        return encodeBase64(combined)
    }

    override fun sha256(inputBase64: String): String {
        val bytes = decodeBase64(inputBase64)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    override fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}