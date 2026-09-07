package com.dazcdude.lightmanager.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dazcdude.lightmanager.LightIpKey
import com.dazcdude.lightmanager.LightObject
import com.dazcdude.lightmanager.TurnLightOffAction
import com.dazcdude.lightmanager.TurnLightOnAction

@Composable
fun WidgetComposable(lightObject: LightObject) {
    Column(
        modifier = GlanceModifier
            .padding(4.dp)
            .fillMaxSize()
            .background(GlanceTheme.colors.surface),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lightObject.ip.isEmpty()) {
            Box(modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                Text(
                    text = "Configure widget to use",
                    style = TextStyle(
                        color = ColorProvider(Color.Black, Color.White),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                    text = lightObject.displayName,
                    style = TextStyle(
                        color = ColorProvider(Color.Black, Color.White)
                    ))
                }

                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        text = "On",
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        onClick = actionRunCallback<TurnLightOnAction>(
                            actionParametersOf(
                                LightIpKey to lightObject.ip
                            )
                        )
                    )

                    Spacer(
                        modifier = GlanceModifier.width(8.dp)
                    )

                    Button(
                        text = "Off",
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        onClick = actionRunCallback<TurnLightOffAction>(
                            actionParametersOf(
                                LightIpKey to lightObject.ip
                            )
                        )
                    )
                }
            }
        }
    }
}