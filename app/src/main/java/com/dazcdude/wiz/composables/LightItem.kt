package com.dazcdude.wiz.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dazcdude.wiz.LightObject
import com.dazcdude.wiz.R
import com.dazcdude.wiz.viewmodels.LightItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightItem(lightItemViewModel: LightItemViewModel, lightObject: LightObject) {
    var showSheet by remember { mutableStateOf(false) }
    var brightnessSliderPosition by remember { mutableFloatStateOf(0f) }

    val lightData by lightItemViewModel.lightData.collectAsState()
    var lightOn = lightData[lightObject.ip]?.state ?: false

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Bottom sheet UI
    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showSheet = false }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp, 8.dp)
            ) {
                Text(
                    text = if (lightObject.displayName.isBlank())
                        lightObject.ip
                    else
                        lightObject.displayName
                )

                if (lightObject.displayName.isNotBlank()) {
                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        tonalElevation = 10.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "IP: ${lightObject.ip}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp, 8.dp))
                    }
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LightOnButton { lightItemViewModel.turnBulbOn(lightObject.ip) }

                    Spacer(modifier = Modifier.width(8.dp))

                    LightOffButton { lightItemViewModel.turnBulbOff(lightObject.ip) }

                    Spacer(modifier = Modifier.weight(1f))

                    DeleteButton { lightItemViewModel.removeLight(lightObject.ip) }
                }
            }

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)) {
                Icon(painter = painterResource(R.drawable.ic_brightness),
                    contentDescription = "Brightness"
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Slider(
                    value = brightnessSliderPosition,
                    onValueChange = {brightnessSliderPosition = it},
                    steps = 9,
                    valueRange = 10f..100f,
                    onValueChangeFinished = {
                        lightItemViewModel.setBrightness(lightObject.ip, brightnessSliderPosition.toInt())
                        lightOn = true
                    }
                )
            }
        }
    }

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    showSheet = true
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (lightObject.displayName.isBlank())
                        lightObject.ip
                    else
                        lightObject.displayName
                )

                if (lightObject.displayName.isNotBlank()) {
                    Text(
                        text = lightObject.ip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            LightOffButton { lightItemViewModel.turnBulbOff(lightObject.ip) }

            Spacer(modifier = Modifier.width(8.dp))

            LightOnButton { lightItemViewModel.turnBulbOn(lightObject.ip) }
        }
    }
}

@Composable
fun LightOnButton(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.size(54.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_light_on),
            contentDescription = "Power On",
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}

@Composable
fun LightOffButton(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = Modifier.size(54.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_light_off),
            contentDescription = "Power Off",
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}

@Composable
fun DeleteButton(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier.size(54.dp)
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}