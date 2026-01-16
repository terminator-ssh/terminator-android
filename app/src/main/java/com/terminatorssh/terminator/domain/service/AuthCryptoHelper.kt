package com.terminatorssh.terminator.domain.service

import com.terminatorssh.terminator.domain.common.Base64Helper.encodeBase64

class AuthCryptoHelper(
    private val cryptoService: CryptoService
) {
    data class DerivedKeys(
        val kek: ByteArray,
        val loginKeyBase64: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DerivedKeys

            if (!kek.contentEquals(other.kek)) return false
            if (loginKeyBase64 != other.loginKeyBase64) return false

            return true
        }

        override fun hashCode(): Int {
            var result = kek.contentHashCode()
            result = 31 * result + loginKeyBase64.hashCode()
            return result
        }
    }

    suspend fun deriveKeys(password: String,
                           keySaltBase64: String,
                           authSaltBase64: String): DerivedKeys {

        val kek = cryptoService.deriveKEK(password, keySaltBase64)
        val loginKeyBytes = cryptoService.deriveKEK(password, authSaltBase64)
        val loginKeyBase64 = encodeBase64(loginKeyBytes)

        return DerivedKeys(kek, loginKeyBase64)
    }
}