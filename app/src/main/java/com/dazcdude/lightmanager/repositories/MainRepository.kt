package com.dazcdude.lightmanager.repositories

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.SettingsSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException

class MainRepository(private val sharedPreferences: SharedPreferences)
{
    private val _lights = MutableStateFlow<List<LightObject>>(emptyList())
    val lights: StateFlow<List<LightObject>> = _lights

    init {
        refreshLights()
    }

    fun refreshLights() {
        _lights.value = getSavedLights()
    }

    fun getSavedLights(): List<LightObject> {
        val allSavedLights = mutableListOf<LightObject>()

        for ((_, value) in sharedPreferences.all)
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

    suspend fun searchLight(): List<String> = withContext(Dispatchers.IO) {

        val results = mutableListOf<String>()

        val message = """{"method":"getPilot","params":{}}""".toByteArray()

        val group = InetAddress.getByName("224.0.0.1")

        val multicastSocket = MulticastSocket(SettingsSingleton.PORT)
        multicastSocket.soTimeout = 5000

        multicastSocket.joinGroup(group)

        val datagramPacket = DatagramPacket(message, message.size, group, SettingsSingleton.PORT)

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

                    //Checks if any saved light object already has the same ip
                    if (_lights.value.none { it.ip == light.ip }) {
                        saveLight(light)
                        refreshLights()
                    }
                }

                results.add(
                    String(response.data, 0, response.length)
                )
            } catch (e: SocketTimeoutException) {
                break
            }
        }

        Log.d("Finished Light Search", results.toString())

        return@withContext results
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
}