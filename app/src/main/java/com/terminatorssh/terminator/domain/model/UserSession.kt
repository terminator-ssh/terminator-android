package com.terminatorssh.terminator.domain.model

data class UserSession(
    val userId: String,
    val username: String,
    val masterKey: ByteArray,
    val token: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserSession

        if (userId != other.userId) return false
        if (username != other.username) return false
        if (!masterKey.contentEquals(other.masterKey)) return false
        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + masterKey.contentHashCode()
        result = 31 * result + (token?.hashCode() ?: 0)
        return result
    }
}