package com.dazcdude.lightmanager

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dazcdude.lightmanager.ui.theme.LightManagerTheme
import com.dazcdude.lightmanager.composables.MainComposable
import com.dazcdude.lightmanager.repositories.LightItemRepository
import com.dazcdude.lightmanager.repositories.MainRepository
import com.dazcdude.lightmanager.viewmodels.MainViewModel

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getPreferences(MODE_PRIVATE) ?: return

        val lightItemRepository = LightItemRepository()
        val mainRepository = MainRepository(sharedPref)

        val mainViewModel = MainViewModel(mainRepository, lightItemRepository)

        setContent {
            LightManagerTheme {
                MainComposable(mainViewModel, ::openWifiSettings)
            }
        }
    }

    private fun openWifiSettings() {
        startActivity(Intent(Settings.Panel.ACTION_WIFI))
    }
}