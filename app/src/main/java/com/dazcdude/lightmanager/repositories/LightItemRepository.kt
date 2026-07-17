package com.dazcdude.lightmanager.repositories

import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.dazcdude.lightmanager.LightData
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.SettingsSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException

class LightItemRepository() {
    fun sendUdp(message: String, bulbIp: String) {
        DatagramSocket().use { socket ->
            val address = InetAddress.getByName(bulbIp)

            val packet = DatagramPacket(
                message.toByteArray(),
                message.length,
                address,
                SettingsSingleton.PORT
            )

            socket.send(packet)
        }
    }

    fun turnLightOn(lightIp: String) {
        val json = """
        {
            "method":"setState",
            "params":{
                "state":true
            }
        }
        """.trimIndent()

        sendUdp(json, lightIp)
    }

    fun turnLightOff(lightIp: String) {
        val json = """
        {
            "method":"setState",
            "params":{
                "state":false
            }
        }
        """.trimIndent()

        sendUdp(json, lightIp)
    }

    fun setBrightness(lightIp: String, brightness: Int) {
        val safeBrightness = brightness.coerceIn(10, 100)

        val json = """
        {
            "method": "setPilot",
            "params": {
                "state": true,
                "dimming": $safeBrightness
            }
        }
        """.trimIndent()

        sendUdp(json, lightIp)
    }
}