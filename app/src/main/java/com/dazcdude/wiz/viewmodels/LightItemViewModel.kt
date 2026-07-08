package com.dazcdude.wiz.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dazcdude.wiz.LightData
import com.dazcdude.wiz.LightObject
import com.dazcdude.wiz.repositories.LightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class LightItemViewModel(private val lightRepository: LightRepository) : ViewModel()
{
    private val _lights = MutableStateFlow<List<LightObject>>(emptyList())
    val lights: StateFlow<List<LightObject>> = _lights

    private val _lightData = MutableStateFlow<Map<String, LightData>>(emptyMap())

    val lightData: StateFlow<Map<String, LightData>> = _lightData

    init {
        refreshLights()
    }

    fun searchLight() {
        CoroutineScope(Dispatchers.IO).launch {
            lightRepository.searchLight()
        }
    }

    private fun refreshLights() {
        _lights.value = lightRepository.getSavedLights()

        viewModelScope.launch {
            val dataMap = mutableMapOf<String, LightData>()

            for (light in _lights.value) {
                lightRepository.getLightData(light.ip)?.let { data ->
                    dataMap[light.ip] = data
                }
            }

            _lightData.value = dataMap
        }
    }

    fun isValidIp(ip: String): Boolean {
        return try {
            val address = InetAddress.getByName(ip)
            address.hostAddress == ip
        } catch (e: Exception) {
            false
        }
    }

    fun turnBulbOn(bulbIp: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val json = """
        {
            "method":"setState",
            "params":{
                "state":true
            }
        }
        """.trimIndent()

            lightRepository.sendUdp(json, bulbIp)
        }
    }

    fun turnBulbOff(bulbIp: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val json = """
        {
            "method":"setState",
            "params":{
                "state":false
            }
        }
        """.trimIndent()

            lightRepository.sendUdp(json, bulbIp)
        }
    }

    fun setBrightness(bulbIp: String, brightness: Int) {
        viewModelScope.launch(Dispatchers.IO) {

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

            lightRepository.sendUdp(json, bulbIp)
        }
    }

    fun saveLight(ip: String, displayName: String) {
        val light = LightObject(
            ip = ip,
            displayName = displayName
        )
        lightRepository.saveLight(light)

        refreshLights()
    }

    fun removeLight(ip: String) {
        val light = LightObject(
            ip = ip,
            displayName = ""
        )
        lightRepository.removeLight(light)

        refreshLights()
    }

    fun getSavedLights(): List<LightObject> {
        return lightRepository.getSavedLights()
    }
}