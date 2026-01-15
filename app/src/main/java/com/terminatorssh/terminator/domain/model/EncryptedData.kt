package com.terminatorssh.terminator.domain.model

data class EncryptedData(
    val cipherText: String,
    val iv: String,
    val tag: String
)