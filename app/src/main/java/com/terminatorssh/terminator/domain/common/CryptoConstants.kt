package com.terminatorssh.terminator.domain.common

object CryptoConstants {
    const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    const val GCM_TAG_LENGTH = 128
    const val GCM_IV_LENGTH = 12
    const val ITERATIONS = 3
    const val MEMORY_COST = 131072
    const val PARALLELISM = 4
    const val HASH_LENGTH = 32
}