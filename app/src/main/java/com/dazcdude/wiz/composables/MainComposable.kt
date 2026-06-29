package com.dazcdude.wiz.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.dazcdude.wiz.viewmodels.LightItemViewModel

@Composable
fun MainComposable(lightItemViewModel: LightItemViewModel) {
    val lights by lightItemViewModel.lights.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveIP by remember { mutableStateOf("") }
    var saveDisplayName by remember { mutableStateOf("") }

    val ipValid = saveIP.isBlank() || lightItemViewModel.isValidIp(saveIP)

    Scaffold(
//        bottomBar = {
//        BottomAppBar(
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
//            contentColor = MaterialTheme.colorScheme.primary,
//        ) {
//            IconButton(onClick = {lightItemViewModel.scanLights()}) {
//                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
//            }
//        }
//    },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSaveDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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