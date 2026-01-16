package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.RegisterRequest
import com.terminatorssh.terminator.data.remote.service.UserPersistenceService
import com.terminatorssh.terminator.domain.common.Base64Helper.encodeBase64
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterUseCase(
    private val clientFactory: RetrofitClientFactory,
    private val cryptoService: CryptoService,
    private val authCryptoHelper: AuthCryptoHelper,
    private val userPersistenceService: UserPersistenceService
) {
    suspend operator fun invoke(url: String, username: String, pass: String): Result<UserSession>
        = withContext(Dispatchers.IO) {
        val keySalt = encodeBase64(cryptoService.generateKeySalt())
        val authSalt = encodeBase64(cryptoService.generateAuthSalt())
        val masterKey = cryptoService.generateMasterKey()

        val keys = authCryptoHelper.deriveKeys(pass, keySalt, authSalt)
        val loginHash = cryptoService.sha256(keys.loginKeyBase64)

        val encMKData = cryptoService.encryptAES(masterKey, keys.kek)
        val packedMK = cryptoService.packBlob(encMKData)

        val api = clientFactory.create(url)
        val regRes = api.register(RegisterRequest(username, authSalt, keySalt, packedMK, keys.loginKeyBase64))

        userPersistenceService.saveAndActivateNewSession(
            username = username,
            keySalt = keySalt,
            authSalt = authSalt,
            encryptedMasterKey = packedMK,
            loginHash = loginHash,
            serverUrl = url,
            masterKeyBytes = masterKey,
            token = regRes.accessToken
        )
    }
}