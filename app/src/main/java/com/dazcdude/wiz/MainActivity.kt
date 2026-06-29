package com.dazcdude.wiz

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dazcdude.wiz.ui.theme.WiZManagerTheme
import com.dazcdude.wiz.composables.MainComposable
import com.dazcdude.wiz.repositories.LightRepository
import com.dazcdude.wiz.viewmodels.LightItemViewModel

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getPreferences(MODE_PRIVATE) ?: return

        val lightRepository = LightRepository(sharedPref)
        val lightItemViewModel = LightItemViewModel(lightRepository)

        setContent {
            WiZManagerTheme {
                MainComposable(lightItemViewModel, ::openWifiSettings)
            }
        }
    }

    private fun openWifiSettings() {
        startActivity(Intent(Settings.Panel.ACTION_WIFI))
    }
}