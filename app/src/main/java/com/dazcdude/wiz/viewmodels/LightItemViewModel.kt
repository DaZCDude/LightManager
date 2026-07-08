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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class LightItemViewModel(private val lightRepository: LightRepository) : ViewModel()
{
    val lights = lightRepository.lights

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _lightData = MutableStateFlow<Map<String, LightData>>(emptyMap())
    val lightData: StateFlow<Map<String, LightData>> = _lightData

    init {
        viewModelScope.launch {
            lights.collect { lights ->
                val data = lights.associate { light ->
                    light.ip to lightRepository.getLightData(light.ip)!!
                }
                _lightData.value = data
            }
        }
    }

    fun searchLight() {
        viewModelScope.launch {
            _isSearching.value = true

            lightRepository.searchLight()

            _isSearching.value = false
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

        lightRepository.refreshLights()
    }

    fun removeLight(ip: String) {
        val light = LightObject(
            ip = ip,
            displayName = ""
        )
        lightRepository.removeLight(light)

        lightRepository.refreshLights()
    }

    fun getSavedLights(): List<LightObject> {
        return lightRepository.getSavedLights()
    }
}