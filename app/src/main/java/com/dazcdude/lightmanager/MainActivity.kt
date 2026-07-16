package com.dazcdude.lightmanager

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dazcdude.lightmanager.ui.theme.LightManagerTheme
import com.dazcdude.lightmanager.composables.MainComposable
import com.dazcdude.lightmanager.repositories.LightRepository
import com.dazcdude.lightmanager.viewmodels.LightItemViewModel

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getPreferences(MODE_PRIVATE) ?: return

        val wifi = getSystemService(WIFI_SERVICE) as WifiManager

        val lightRepository = LightRepository(sharedPref, wifi)
        val lightItemViewModel = LightItemViewModel(lightRepository)

        setContent {
            LightManagerTheme {
                MainComposable(lightItemViewModel, ::openWifiSettings)
            }
        }
    }

    private fun openWifiSettings() {
        startActivity(Intent(Settings.Panel.ACTION_WIFI))
    }
}