package com.dazcdude.lightmanager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.json.JSONObject

val LightIpKey = ActionParameters.Key<String>("light_ip")

class AppWidget: GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {
            val prefs = currentState<Preferences>()

            val savedLight = prefs[stringPreferencesKey("selected_light")]

            val light = if (savedLight != null) {
                val json = JSONObject(savedLight)

                LightObject(
                    ip = json.getString("ip"),
                    displayName = json.getString("display_name")
                )
            } else {
                LightObject("", "")
            }

            MyContent(light)
        }
    }

    @Composable
    private fun MyContent(lightObject: LightObject) {
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
                    Text(
                        text = lightObject.displayName,
                        style = TextStyle(
                            color = ColorProvider(Color.Black, Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Row(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.padding(8.dp)) {
                        Button(
                            text = "On",
                            onClick = actionRunCallback<TurnLightOnAction>(
                                actionParametersOf(
                                    LightIpKey to lightObject.ip
                                )
                            )
                        )

                        Spacer(modifier = GlanceModifier.padding(4.dp))

                        Button(
                            text = "Off",
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
}
class TurnLightOnAction : ActionCallback
{
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val ip = parameters[LightIpKey] ?: return

        LightController.turnLightOn(ip)
    }
}
class TurnLightOffAction : ActionCallback
{
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val ip = parameters[LightIpKey] ?: return

        LightController.turnLightOff(ip)
    }
}