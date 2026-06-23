package com.dazcdude.wiz.repositories

import android.content.SharedPreferences
import android.util.Log
import com.dazcdude.wiz.LightObject
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import androidx.core.content.edit

class LightRepository(private val sharedPreferences: SharedPreferences) {
    fun sendUdp(message: String, bulbIp: String) {
        DatagramSocket().use { socket ->
            val address = InetAddress.getByName(bulbIp)

            val packet = DatagramPacket(
                message.toByteArray(),
                message.length,
                address,
                38899
            )

            socket.send(packet)
        }
    }

    fun saveLight(lightObject: LightObject) {
        val json = JSONObject().apply {
            put("ip", lightObject.ip)
            put("display_name", lightObject.displayName)
        }

        sharedPreferences.edit()
        {
            putString(lightObject.ip, json.toString())
        }
    }

    fun removeLight(lightObject: LightObject) {
        sharedPreferences.edit {
            remove(lightObject.ip)
        }
    }

    fun getSavedLights(): List<LightObject> {
        val allSavedLights = mutableListOf<LightObject>()

        for ((key, value) in sharedPreferences.all)
        {
            //Only accept saved app if package name is string
            if (value is String)
            {
                try
                {
                    //Get and Set App Variables
                    val json = JSONObject(value)

                    val lightIP: String = json.getString("ip")
                    val lightDisplayName: String = json.getString("display_name")

                    //Setup app
                    val app = LightObject(
                        ip = lightIP,
                        displayName = lightDisplayName
                    )

                    allSavedLights.add(app)
                }
                //Skip corrupt apps
                catch (e: Exception) {
                    Log.d("ERROR", e.toString())
                }
            }
        }

        return allSavedLights
    }
}