package com.dazcdude.wiz.composables

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
import com.dazcdude.wiz.viewmodels.LightItemViewModel

@Composable
fun SaveLightDialog(lightItemViewModel: LightItemViewModel, onDismissRequest: () -> Unit) {
    var saveIP by remember { mutableStateOf("") }
    val ipValid = saveIP.isBlank() || lightItemViewModel.isValidIp(saveIP)
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
                    lightItemViewModel.saveLight(saveIP, saveDisplayName)

                    saveIP = ""
                    saveDisplayName = ""

                    onDismissRequest()
                },
                enabled = lightItemViewModel.isValidIp(saveIP) && saveDisplayName.isNotBlank()
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