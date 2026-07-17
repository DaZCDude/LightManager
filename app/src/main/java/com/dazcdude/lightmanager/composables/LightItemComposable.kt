package com.dazcdude.lightmanager.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.R
import com.dazcdude.lightmanager.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightItem(mainViewModel: MainViewModel, lightObject: LightObject) {
    var showSheet by remember { mutableStateOf(false) }
    var brightnessSliderPosition by remember { mutableFloatStateOf(0f) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val lightData by mainViewModel.lightItemData.collectAsState()

    LaunchedEffect(lightObject.ip) {
        mainViewModel.loadLightItemData(lightObject.ip)
    }

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
                    text = lightObject.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    tonalElevation = 10.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "IP: ${lightObject.ip}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp, 8.dp))
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LightOnButton { mainViewModel.turnLightOn(lightObject.ip) }

                    Spacer(modifier = Modifier.width(8.dp))

                    LightOffButton { mainViewModel.turnLightOff(lightObject.ip) }

                    Spacer(modifier = Modifier.weight(1f))

                    DeleteButton { mainViewModel.removeLight(lightObject.ip) }
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
                        mainViewModel.setLightBrightness(lightObject.ip, brightnessSliderPosition.toInt())
                    }
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(onClick = {showSheet = true})
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = lightObject.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = lightObject.ip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.weight(1f))

        LightOffButton { mainViewModel.turnLightOff(lightObject.ip) }

        Spacer(modifier = Modifier.width(8.dp))

        LightOnButton { mainViewModel.turnLightOn(lightObject.ip) }
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