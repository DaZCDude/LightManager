package com.dazcdude.lightmanager

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object LightController
{
    private fun sendUdp(message: String, lightIp: String) {
        DatagramSocket().use { socket ->
            val address = InetAddress.getByName(lightIp)

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