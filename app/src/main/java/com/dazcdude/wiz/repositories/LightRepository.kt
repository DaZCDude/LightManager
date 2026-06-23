package com.dazcdude.wiz.repositories

import android.content.SharedPreferences
import android.util.Log
import com.dazcdude.wiz.LightObject
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import androidx.core.content.edit
import com.dazcdude.wiz.LightData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

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

    suspend fun getLightData(bulbIp: String): LightData? =
        withContext(Dispatchers.IO) {

            try {
                val request = """{"method":"getPilot","params":{}}"""

                DatagramSocket().use { socket ->
                    socket.soTimeout = 3000

                    val address = InetAddress.getByName(bulbIp)

                    val sendPacket = DatagramPacket(
                        request.toByteArray(),
                        request.toByteArray().size,
                        address,
                        38899
                    )

                    socket.send(sendPacket)

                    Log.d("WIZ", "Sent getPilot to $bulbIp")

                    val buffer = ByteArray(1024)
                    val receivePacket = DatagramPacket(buffer, buffer.size)

                    socket.receive(receivePacket)

                    val response = String(
                        receivePacket.data,
                        0,
                        receivePacket.length
                    )

                    Log.d("WIZ", response)

                    val json = JSONObject(response)
                    val result = json.getJSONObject("result")

                    val lightData = LightData(
                        state = result.getBoolean("state"),
                        dimming = result.getInt("dimming")
                    )

                    return@withContext lightData
                }
            } catch (e: SocketTimeoutException) {
                Log.e("WIZ", "Timed out waiting for response from $bulbIp")
                return@withContext null
            } catch (e: Exception) {
                Log.e("WIZ", "Error talking to bulb", e)
                return@withContext null
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