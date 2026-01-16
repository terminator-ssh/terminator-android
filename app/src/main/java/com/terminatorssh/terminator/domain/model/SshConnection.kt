package com.terminatorssh.terminator.domain.model

import java.io.InputStream
import java.io.OutputStream

data class SshConnection(
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val session: Any? = null
)