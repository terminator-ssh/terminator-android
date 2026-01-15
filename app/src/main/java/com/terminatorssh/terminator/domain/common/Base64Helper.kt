package com.terminatorssh.terminator.domain.common

import android.util.Base64

object Base64Helper {
    fun encodeBase64(byteArray: ByteArray) : String
        = Base64.encodeToString(byteArray, Base64.NO_WRAP)

    fun decodeBase64(str: String) : ByteArray
        = Base64.decode(str, Base64.NO_WRAP)
}