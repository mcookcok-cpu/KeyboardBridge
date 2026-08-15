package com.julianto.keyboardbridge

import android.content.Context
import com.kunal52.AndroidRemoteContext
import com.kunal52.exception.PairingException
import com.kunal52.pairing.PairingListener
import com.kunal52.pairing.PairingSession
import com.kunal52.remote.RemoteSession
import com.kunal52.remote.Remotemessage
import java.io.File
import java.util.concurrent.Executors

class TvBridge(private val context: Context, private val callback: Callback) {
    interface Callback {
        fun onStatus(message: String)
        fun onPairingCodeRequired()
        fun onConnected()
        fun onError(message: String)
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var pairing: PairingSession? = null
    private var remote: RemoteSession? = null

    fun connect(host: String) {
        executor.execute {
            try {
                val ctx = AndroidRemoteContext.getInstance()
                ctx.clientName = "Keyboard Bridge"
                ctx.serviceName = "julianto-keyboard-bridge"
                ctx.keyStoreFile = File(context.filesDir, "androidtv.keystore")
                callback.onStatus("Menghubungkan ke $host…")

                val remoteSession = RemoteSession(host, 6466, object : RemoteSession.RemoteSessionListener {
                    override fun onConnected() {
                        callback.onConnected()
                    }
                    override fun onSslError() { callback.onError("TLS remote gagal") }
                    override fun onDisconnected() { callback.onStatus("TV terputus") }
                    override fun onError(message: String) { callback.onError(message.ifBlank { "Remote error" }) }
                })
                remote = remoteSession

                if (ctx.keyStoreFile.exists()) {
                    remoteSession.connect()
                } else {
                    callback.onStatus("Pairing: lihat PIN di TV")
                    val p = PairingSession()
                    pairing = p
                    p.pair(host, 6467, object : PairingListener {
                        override fun onSessionCreated() {}
                        override fun onPerformInputDeviceRole() {}
                        override fun onPerformOutputDeviceRole(gamma: ByteArray?) {}
                        override fun onSecretRequested() { callback.onPairingCodeRequired() }
                        override fun onSessionEnded() {}
                        override fun onError(message: String) { callback.onError(message) }
                        override fun onPaired() {
                            callback.onStatus("Pairing berhasil, menyambungkan remote…")
                            try { remoteSession.connect() } catch (e: Exception) { callback.onError(e.message ?: "Gagal konek remote") }
                        }
                        override fun onLog(message: String) {}
                    })
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    fun providePin(pin: String) {
        executor.execute {
            try { pairing?.provideSecret(pin) ?: callback.onError("Sesi pairing belum aktif") }
            catch (e: Exception) { callback.onError(e.message ?: "PIN tidak valid") }
        }
    }

    fun send(keyCode: Int) {
        val r = remote ?: return
        try {
            val code = mapKey(keyCode) ?: return
            r.sendCommand(code, Remotemessage.RemoteDirection.SHORT)
        } catch (e: Exception) {
            callback.onError(e.message ?: "Gagal mengirim tombol")
        }
    }

    private fun mapKey(k: Int): Remotemessage.RemoteKeyCode? = when (k) {
        android.view.KeyEvent.KEYCODE_DPAD_UP -> Remotemessage.RemoteKeyCode.KEYCODE_DPAD_UP
        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> Remotemessage.RemoteKeyCode.KEYCODE_DPAD_DOWN
        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> Remotemessage.RemoteKeyCode.KEYCODE_DPAD_LEFT
        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> Remotemessage.RemoteKeyCode.KEYCODE_DPAD_RIGHT
        android.view.KeyEvent.KEYCODE_DPAD_CENTER, android.view.KeyEvent.KEYCODE_ENTER -> Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER
        android.view.KeyEvent.KEYCODE_BACK, android.view.KeyEvent.KEYCODE_ESCAPE -> Remotemessage.RemoteKeyCode.KEYCODE_BACK
        android.view.KeyEvent.KEYCODE_HOME -> Remotemessage.RemoteKeyCode.KEYCODE_HOME
        android.view.KeyEvent.KEYCODE_VOLUME_UP -> Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP
        android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_DOWN
        android.view.KeyEvent.KEYCODE_VOLUME_MUTE -> Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_MUTE
        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> Remotemessage.RemoteKeyCode.KEYCODE_MEDIA_PLAY_PAUSE
        android.view.KeyEvent.KEYCODE_MEDIA_NEXT -> Remotemessage.RemoteKeyCode.KEYCODE_MEDIA_NEXT
        android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS -> Remotemessage.RemoteKeyCode.KEYCODE_MEDIA_PREVIOUS
        android.view.KeyEvent.KEYCODE_MENU -> Remotemessage.RemoteKeyCode.KEYCODE_MENU
        android.view.KeyEvent.KEYCODE_0 -> Remotemessage.RemoteKeyCode.KEYCODE_0
        android.view.KeyEvent.KEYCODE_1 -> Remotemessage.RemoteKeyCode.KEYCODE_1
        android.view.KeyEvent.KEYCODE_2 -> Remotemessage.RemoteKeyCode.KEYCODE_2
        android.view.KeyEvent.KEYCODE_3 -> Remotemessage.RemoteKeyCode.KEYCODE_3
        android.view.KeyEvent.KEYCODE_4 -> Remotemessage.RemoteKeyCode.KEYCODE_4
        android.view.KeyEvent.KEYCODE_5 -> Remotemessage.RemoteKeyCode.KEYCODE_5
        android.view.KeyEvent.KEYCODE_6 -> Remotemessage.RemoteKeyCode.KEYCODE_6
        android.view.KeyEvent.KEYCODE_7 -> Remotemessage.RemoteKeyCode.KEYCODE_7
        android.view.KeyEvent.KEYCODE_8 -> Remotemessage.RemoteKeyCode.KEYCODE_8
        android.view.KeyEvent.KEYCODE_9 -> Remotemessage.RemoteKeyCode.KEYCODE_9
        android.view.KeyEvent.KEYCODE_A -> Remotemessage.RemoteKeyCode.KEYCODE_A
        android.view.KeyEvent.KEYCODE_B -> Remotemessage.RemoteKeyCode.KEYCODE_B
        android.view.KeyEvent.KEYCODE_C -> Remotemessage.RemoteKeyCode.KEYCODE_C
        android.view.KeyEvent.KEYCODE_D -> Remotemessage.RemoteKeyCode.KEYCODE_D
        android.view.KeyEvent.KEYCODE_E -> Remotemessage.RemoteKeyCode.KEYCODE_E
        android.view.KeyEvent.KEYCODE_F -> Remotemessage.RemoteKeyCode.KEYCODE_F
        android.view.KeyEvent.KEYCODE_G -> Remotemessage.RemoteKeyCode.KEYCODE_G
        android.view.KeyEvent.KEYCODE_H -> Remotemessage.RemoteKeyCode.KEYCODE_H
        android.view.KeyEvent.KEYCODE_I -> Remotemessage.RemoteKeyCode.KEYCODE_I
        android.view.KeyEvent.KEYCODE_J -> Remotemessage.RemoteKeyCode.KEYCODE_J
        android.view.KeyEvent.KEYCODE_K -> Remotemessage.RemoteKeyCode.KEYCODE_K
        android.view.KeyEvent.KEYCODE_L -> Remotemessage.RemoteKeyCode.KEYCODE_L
        android.view.KeyEvent.KEYCODE_M -> Remotemessage.RemoteKeyCode.KEYCODE_M
        android.view.KeyEvent.KEYCODE_N -> Remotemessage.RemoteKeyCode.KEYCODE_N
        android.view.KeyEvent.KEYCODE_O -> Remotemessage.RemoteKeyCode.KEYCODE_O
        android.view.KeyEvent.KEYCODE_P -> Remotemessage.RemoteKeyCode.KEYCODE_P
        android.view.KeyEvent.KEYCODE_Q -> Remotemessage.RemoteKeyCode.KEYCODE_Q
        android.view.KeyEvent.KEYCODE_R -> Remotemessage.RemoteKeyCode.KEYCODE_R
        android.view.KeyEvent.KEYCODE_S -> Remotemessage.RemoteKeyCode.KEYCODE_S
        android.view.KeyEvent.KEYCODE_T -> Remotemessage.RemoteKeyCode.KEYCODE_T
        android.view.KeyEvent.KEYCODE_U -> Remotemessage.RemoteKeyCode.KEYCODE_U
        android.view.KeyEvent.KEYCODE_V -> Remotemessage.RemoteKeyCode.KEYCODE_V
        android.view.KeyEvent.KEYCODE_W -> Remotemessage.RemoteKeyCode.KEYCODE_W
        android.view.KeyEvent.KEYCODE_X -> Remotemessage.RemoteKeyCode.KEYCODE_X
        android.view.KeyEvent.KEYCODE_Y -> Remotemessage.RemoteKeyCode.KEYCODE_Y
        android.view.KeyEvent.KEYCODE_Z -> Remotemessage.RemoteKeyCode.KEYCODE_Z
        else -> null
    }

    fun close() { executor.shutdownNow() }
}
