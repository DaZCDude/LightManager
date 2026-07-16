package com.dazcdude.lightmanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dazcdude.lightmanager.LightData
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.repositories.LightItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class LightItemViewModel(private val lightItemRepository: LightItemRepository) : ViewModel()
{
    val lights = lightItemRepository.lights

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _lightData = MutableStateFlow<Map<String, LightData>>(emptyMap())
    val lightData: StateFlow<Map<String, LightData>> = _lightData

    init {
        viewModelScope.launch {
            lights.collect { lights ->
                val data = lights.associate { light ->
                    light.ip to lightItemRepository.getLightData(light.ip)!!
                }
                _lightData.value = data
            }
        }
    }

    fun searchLight() {
        viewModelScope.launch {
            _isSearching.value = true

            lightItemRepository.searchLight()

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

            lightItemRepository.sendUdp(json, bulbIp)
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

            lightItemRepository.sendUdp(json, bulbIp)
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

            lightItemRepository.sendUdp(json, bulbIp)
        }
    }

    fun saveLight(ip: String, displayName: String) {
        val light = LightObject(
            ip = ip,
            displayName = displayName
        )
        lightItemRepository.saveLight(light)

        lightItemRepository.refreshLights()
    }

    fun removeLight(ip: String) {
        val light = LightObject(
            ip = ip,
            displayName = ""
        )
        lightItemRepository.removeLight(light)

        lightItemRepository.refreshLights()
    }

    fun getSavedLights(): List<LightObject> {
        return lightItemRepository.getSavedLights()
    }
}