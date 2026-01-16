package com.terminatorssh.terminator.data.remote.dto


data class RegisterRequest(
    val username: String,
    val authSalt: String,
    val keySalt: String,
    val encryptedMasterKey: String,
    val loginKey: String)

data class RegisterResponse(
    val accessToken: String)