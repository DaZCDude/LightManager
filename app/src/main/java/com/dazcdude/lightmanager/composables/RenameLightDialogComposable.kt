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
fun RenameLightDialogComposable(mainViewModel: MainViewModel, onDismissRequest: () -> Unit, lightIp: String) {
    var saveDisplayName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Rename Light") },
        text = {
            Column {
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
                    mainViewModel.saveLight(lightIp, saveDisplayName)

                    saveDisplayName = ""

                    onDismissRequest()
                }
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