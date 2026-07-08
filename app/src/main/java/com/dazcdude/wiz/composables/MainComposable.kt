package com.dazcdude.wiz.composables

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dazcdude.wiz.viewmodels.LightItemViewModel

@Composable
fun MainComposable(lightItemViewModel: LightItemViewModel, openWifiSettings:() -> Unit) {
    val lights by lightItemViewModel.lights.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveIP by remember { mutableStateOf("") }
    var saveDisplayName by remember { mutableStateOf("") }

    val ipValid = saveIP.isBlank() || lightItemViewModel.isValidIp(saveIP)

    val wifiEnabled = rememberWifiEnabled()

    if (!wifiEnabled) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = openWifiSettings) {
                    Text("Enable Wi-Fi")
                }
            },
            title = {
                Text("No Wi-Fi")
            },
            text = {
                Text("The app uses Wi-Fi to call light commands.")
            }
        )
    }

    if (lightItemViewModel.isSearching.collectAsState().value) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Searching for lights")
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(onClick = { lightItemViewModel.searchLight() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                FloatingActionButton(onClick = { showSaveDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
                .clip(
                    RoundedCornerShape(
                        16.dp,
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lights) { light ->
                LightItem(
                    lightItemViewModel = lightItemViewModel,
                    lightObject = light
                )
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Add Light") },
                text = {
                    Column {
                        Text("IP Address")

                        TextField(
                            value = saveIP,
                            onValueChange = {saveIP = it},
                            singleLine = true,
                            isError = !ipValid,
                            supportingText = {
                                if (!ipValid) {
                                    Text("Invalid IP Address!")
                                }
                            })

                        Text("Display Name")

                        TextField(
                            value = saveDisplayName,
                            onValueChange = {saveDisplayName = it},
                            singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            lightItemViewModel.saveLight(saveIP, saveDisplayName)

                            saveIP = ""
                            saveDisplayName = ""

                            showSaveDialog = false
                        },
                        enabled = lightItemViewModel.isValidIp(saveIP) && saveDisplayName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSaveDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun rememberWifiEnabled(): Boolean {
    val context = LocalContext.current
    val wifi = remember {
        context.getSystemService(WifiManager::class.java)
    }

    var enabled by remember {
        mutableStateOf(wifi?.isWifiEnabled == true)
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                enabled = wifi?.isWifiEnabled == true
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return enabled
}