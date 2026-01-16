package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.PreflightRequest
import com.terminatorssh.terminator.domain.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SmartConnectUseCase(
    private val clientFactory: RetrofitClientFactory,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) {
    suspend operator fun invoke(url: String, username: String, pass: String): Result<UserSession>
    = withContext(Dispatchers.IO) {
        try {
            val api = clientFactory.create(url)

            try {
                val preflight = api.preflight(PreflightRequest(username))

                return@withContext loginUseCase(url, username, pass)

            } catch (e: HttpException) {
                if (e.code() == 400) {
                    return@withContext registerUseCase(url, username, pass)
                }
                throw e
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}