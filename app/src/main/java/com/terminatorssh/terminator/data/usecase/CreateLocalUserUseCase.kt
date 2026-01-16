package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.remote.service.UserPersistenceService
import com.terminatorssh.terminator.domain.common.Base64Helper.encodeBase64
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateLocalUserUseCase(
    private val cryptoService: CryptoService,
    private val authCryptoHelper: AuthCryptoHelper,
    private val userPersistenceService: UserPersistenceService
) {
    suspend operator fun invoke(username: String, pass: String): Result<Unit>
        = withContext(Dispatchers.IO) {
        try {
            val keySalt = encodeBase64(cryptoService.generateKeySalt())
            val authSalt = encodeBase64(cryptoService.generateAuthSalt())
            val masterKey = cryptoService.generateMasterKey()

            val keys = authCryptoHelper.deriveKeys(
                pass,
                keySalt,
                authSalt)
            val loginHash = cryptoService.sha256(keys.loginKeyBase64)

            val encMKData = cryptoService.encryptAES(masterKey, keys.kek)
            val packedMK = cryptoService.packBlob(encMKData)

            userPersistenceService.saveAndActivateNewSession(
                username = username,
                keySalt = keySalt,
                authSalt = authSalt,
                encryptedMasterKey = packedMK,
                loginHash = loginHash,
                serverUrl = null,
                masterKeyBytes = masterKey,
                token = null
            )

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}