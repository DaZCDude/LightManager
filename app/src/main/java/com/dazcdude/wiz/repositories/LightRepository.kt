package com.dazcdude.wiz.repositories

import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.edit
import com.dazcdude.wiz.LightData
import com.dazcdude.wiz.LightObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException

class LightRepository(private val sharedPreferences: SharedPreferences, private val wifi: WifiManager) {
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

    suspend fun searchLight(): List<String> = withContext(Dispatchers.IO) {

        val results = mutableListOf<String>()

        val message = """{"method":"getPilot","params":{}}""".toByteArray()

        val group = InetAddress.getByName("224.0.0.1")

        Log.d("Multicast Adress", getBroadcastAddress().hostAddress)

        val multicastSocket = MulticastSocket(38899)

        multicastSocket.joinGroup(group)

        val datagramPacket = DatagramPacket(message, message.size, group, 38899)

        multicastSocket.send(datagramPacket)

        while (true) {
            try {
                val buffer = ByteArray(1024)
                val response = DatagramPacket(buffer, buffer.size)
                multicastSocket.receive(response)

                Log.d("Direct MSG from" + response.address, String(response.data, 0, response.length))

                if (String(response.data, 0, response.length) != "{\"method\":\"getPilot\",\"params\":{}}") {
                    val light = LightObject(
                        ip = response.address.hostAddress,
                        displayName = response.address.hostName
                    )
                    saveLight(light)
                }

                results.add(
                    String(response.data, 0, response.length)
                )
            } catch (e: SocketTimeoutException) {
                break
            }
        }

//        val receivedMessage = ByteArray(1000)
//        val receiveDatagramPacket = DatagramPacket(receivedMessage, receivedMessage.size)
//        multicastSocket.receive(receiveDatagramPacket)

        Log.d("Multicast Messages", results.toString())

        return@withContext results
    }

    fun getBroadcastAddress(): InetAddress {
        val interfaces = NetworkInterface.getNetworkInterfaces()

        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()

            if (!networkInterface.isUp || networkInterface.isLoopback) continue

            networkInterface.interfaceAddresses.forEach { addr ->
                val broadcast = addr.broadcast
                if (broadcast != null) {
                    return broadcast
                }
            }
        }

        throw IllegalStateException("No broadcast address found")
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