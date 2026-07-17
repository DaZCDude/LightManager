package com.dazcdude.lightmanager.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dazcdude.lightmanager.viewmodels.MainViewModel

@Composable
fun SaveLightDialog(mainViewModel: MainViewModel, onDismissRequest: () -> Unit) {
    var saveIP by remember { mutableStateOf("") }
    val ipValid = saveIP.isBlank() || mainViewModel.isValidIp(saveIP)
    var saveDisplayName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                    mainViewModel.saveLight(saveIP, saveDisplayName)

                    saveIP = ""
                    saveDisplayName = ""

                    onDismissRequest()
                },
                enabled = mainViewModel.isValidIp(saveIP) && saveDisplayName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}