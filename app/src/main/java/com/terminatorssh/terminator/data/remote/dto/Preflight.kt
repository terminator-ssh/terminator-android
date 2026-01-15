package com.terminatorssh.terminator.data.remote.dto

data class PreflightRequest(
    val username: String)

data class PreflightResponse(
    val authSalt: String,
    val keySalt: String,
    val encryptedMasterKey: String)