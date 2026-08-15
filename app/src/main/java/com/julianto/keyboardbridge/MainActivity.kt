package com.julianto.keyboardbridge

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var tvIp: EditText
    private lateinit var connectButton: Button
    private var bridge: TvBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBtPermissions()
        buildUi()
        bridge = TvBridge(this, object : TvBridge.Callback {
            override fun onStatus(message: String) = runOnUiThread { status.text = "● $message" }
            override fun onConnected() = runOnUiThread { status.text = "● TV TERHUBUNG — keyboard siap" }
            override fun onError(message: String) = runOnUiThread { status.text = "● Error: $message" }
            override fun onPairingCodeRequired() = runOnUiThread { askPin() }
        })
    }

    private fun requestBtPermissions() {
        if (Build.VERSION.SDK_INT >= 31) requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 10)
    }

    private fun buildUi() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 36, 28, 28); setBackgroundColor(Color.rgb(22,22,22)) }
        fun label(t: String, s: Float = 16f) = TextView(this).apply { text=t; textSize=s; setTextColor(Color.WHITE); setPadding(0,10,0,10) }
        box.addView(label("KEYBOARD BRIDGE", 28f))
        box.addView(label("Bluetooth Keyboard  →  Wi‑Fi  →  Android TV", 15f))
        status = label("● Siap", 17f); status.setTextColor(Color.rgb(80,220,120)); box.addView(status)
        tvIp = EditText(this).apply { hint="IP TV, contoh 192.168.1.20"; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY); inputType=android.text.InputType.TYPE_CLASS_PHONE; setSingleLine() }
        box.addView(tvIp)
        connectButton = Button(this).apply { text="SAMBUNGKAN TV"; setOnClickListener { val ip=tvIp.text.toString().trim(); if(ip.isNotEmpty()) bridge?.connect(ip) else status.text="● Masukkan IP TV" } }
        box.addView(connectButton)
        box.addView(label("1. Pastikan HP dan TV berada di Wi‑Fi yang sama.\n2. Masukkan IP TV.\n3. Tekan Sambungkan.\n4. Saat TV menampilkan PIN, masukkan PIN di dialog.\n5. Setelah terhubung, keyboard Bluetooth ke HP dapat mengontrol TV.", 14f))
        box.addView(label("Tombol: ↑ ↓ ← → Enter, Back, Home, Volume, media, angka dan A–Z.", 14f))
        setContentView(box)
    }

    private fun askPin() {
        val input = EditText(this).apply { hint="PIN dari TV"; inputType=android.text.InputType.TYPE_CLASS_NUMBER; setSingleLine() }
        AlertDialogBuilder.show(this, input) { pin -> bridge?.providePin(pin) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        bridge?.send(keyCode)
        status.text = "● ${KeyEvent.keyCodeToString(keyCode)}"
        return true
    }

    override fun onDestroy() { bridge?.close(); super.onDestroy() }
}

object AlertDialogBuilder {
    fun show(activity: Activity, input: EditText, onOk: (String)->Unit) {
        android.app.AlertDialog.Builder(activity).setTitle("Pairing TV").setMessage("Masukkan PIN yang muncul di TV").setView(input)
            .setPositiveButton("PAIR") { _, _ -> onOk(input.text.toString().trim()) }
            .setNegativeButton("Batal", null).show()
    }
}
