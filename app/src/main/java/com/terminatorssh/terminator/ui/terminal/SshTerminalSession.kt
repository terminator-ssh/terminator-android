package com.terminatorssh.terminator.ui.terminal

import jackpal.androidterm.emulatorview.ColorScheme
import jackpal.androidterm.emulatorview.TermSession
import java.io.InputStream
import java.io.OutputStream

class SshTerminalSession(
    private val sshInput: InputStream,
    private val sshOutput: OutputStream
) : TermSession() {

    init {
        setTermIn(sshInput)
        setTermOut(sshOutput)

        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        setColorScheme(ColorScheme(intArrayOf(black, white)))
    }

    override fun finish() {
        try {
            super.finish()
        //} catch (e: NullPointerException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            sshInput.close()
            sshOutput.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}