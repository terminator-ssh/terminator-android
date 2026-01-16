package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.LoginRequest
import com.terminatorssh.terminator.data.remote.dto.PreflightRequest
import com.terminatorssh.terminator.data.remote.service.UserPersistenceService
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginUseCase(
    private val clientFactory: RetrofitClientFactory,
    private val cryptoService: CryptoService,
    private val authCryptoHelper: AuthCryptoHelper,
    private val userPersistenceService: UserPersistenceService
) {
    suspend operator fun invoke(
        url: String,
        username: String,
        pass: String): Result<UserSession>
    = withContext(Dispatchers.IO) {
        try {
            val api = clientFactory.create(url)

            val preflight = api.preflight(PreflightRequest(username))

            val keys = authCryptoHelper.deriveKeys(
                pass,
                preflight.keySalt,
                preflight.authSalt)

            val loginRes = api.login(LoginRequest(username, keys.loginKeyBase64))

            val encMK = cryptoService.unpackBlob(preflight.encryptedMasterKey)
            val mkBytes = cryptoService.decryptAES(encMK, keys.kek)

            val loginHash = cryptoService.sha256(keys.loginKeyBase64)

            userPersistenceService.saveAndActivateNewSession(
                username = username,
                keySalt = preflight.keySalt,
                authSalt = preflight.authSalt,
                encryptedMasterKey = preflight.encryptedMasterKey,
                loginHash = loginHash,
                serverUrl = url,
                masterKeyBytes = mkBytes,
                token = loginRes.accessToken
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}