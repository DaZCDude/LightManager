package com.dazcdude.lightmanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dazcdude.lightmanager.LightData
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.repositories.LightItemRepository
import com.dazcdude.lightmanager.repositories.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class MainViewModel(private val mainRepository: MainRepository, private val lightItemRepository: LightItemRepository): ViewModel()
{
    val lights = mainRepository.lights

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _lightItemData = MutableStateFlow<LightData?>(null)
    val lightItemData: StateFlow<LightData?> = _lightItemData

    fun loadLightItemData(lightIp: String) {
        viewModelScope.launch {
            _lightItemData.value = lightItemRepository.getLightData(lightIp)
        }
    }

    fun searchLight() {
        viewModelScope.launch {
            _isSearching.value = true

            mainRepository.searchLight()

            _isSearching.value = false
        }
    }

    fun saveLight(ip: String, displayName: String) {
        val light = LightObject(
            ip = ip,
            displayName = displayName
        )
        mainRepository.saveLight(light)

        mainRepository.refreshLights()
    }

    fun removeLight(ip: String) {
        val light = LightObject(
            ip = ip,
            displayName = ""
        )
        mainRepository.removeLight(light)

        mainRepository.refreshLights()
    }

    fun isValidIp(ip: String): Boolean {
        return try {
            val address = InetAddress.getByName(ip)
            address.hostAddress == ip
        } catch (e: Exception) {
            false
        }
    }

    fun turnLightOn(lightIp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lightItemRepository.turnLightOn(lightIp)
        }
    }

    fun turnLightOff(lightIp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lightItemRepository.turnLightOff(lightIp)
        }
    }

    fun setLightBrightness(lightIp: String, brightness: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            lightItemRepository.setBrightness(lightIp, brightness)
        }
    }
}