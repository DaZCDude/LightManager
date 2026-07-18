package com.dazcdude.lightmanager.repositories

import android.util.Log
import com.dazcdude.lightmanager.LightData
import com.dazcdude.lightmanager.SettingsSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class LightItemRepository {
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
                        SettingsSingleton.PORT
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
}