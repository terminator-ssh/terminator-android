package com.terminatorssh.terminator.domain.model

data class Host(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String? = null,
    val privateKey: String? = null
)