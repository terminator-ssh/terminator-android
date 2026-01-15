package com.terminatorssh.terminator.data.remote.dto

data class LoginRequest(
    val username: String,
    val loginKey: String)

data class LoginResponse(
    val accessToken: String)