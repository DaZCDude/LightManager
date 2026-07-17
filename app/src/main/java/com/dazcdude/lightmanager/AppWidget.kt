package com.dazcdude.lightmanager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

val LightIpKey = ActionParameters.Key<String>("light_ip")

class AppWidget: GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        val widgetSharedPref = context.getSharedPreferences("widget_light_saved", MODE_PRIVATE) ?: return

        val savedLight = widgetSharedPref.getString("selected_light", null)

        var light = LightObject("", "")

        if (savedLight != null) {
            val json = JSONObject(savedLight)

            light = LightObject(
                ip = json.getString("ip"),
                displayName = json.getString("display_name")
            )
        }

        provideContent {
            // create your AppWidget here
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
                Text(
                    text = lightObject.displayName,
                    style = TextStyle(
                        color = ColorProvider(Color.Black, Color.White)
                    ),
                    modifier = GlanceModifier.padding(top = 8.dp)
                )
                Text(
                    text = lightObject.ip,
                    style = TextStyle(
                        color = ColorProvider(Color.Black, Color.White)
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

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