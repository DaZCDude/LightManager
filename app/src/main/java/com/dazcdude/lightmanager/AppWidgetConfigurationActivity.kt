package com.dazcdude.lightmanager

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.dazcdude.lightmanager.composables.WidgetLightItemComposable
import com.dazcdude.lightmanager.ui.theme.LightManagerTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class AppWidgetConfigurationActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        Log.d("WidgetConfig", "onCreate")

        val sharedPref = getSharedPreferences(SettingsSingleton.SHARED_PREFERENCES_NAME, MODE_PRIVATE) ?: return

        Log.d("WidgetConfig", "Prefs count = ${sharedPref.all.size}")

        setContent {
            LightManagerTheme {
                WidgetComposable(getSavedLights(sharedPref))
            }
        }
    }

    fun getSavedLights(sharedPref: SharedPreferences): List<LightObject> {
        val allSavedLights = mutableListOf<LightObject>()

        for ((_, value) in sharedPref.all)
        {
            //Only accept saved app if package name is string
            if (value is String)
            {
                try
                {
                    //Get and Set App Variables
                    val json = JSONObject(value)

                    val lightIP: String = json.getString("ip")
                    val lightDisplayName: String = json.getString("display_name")

                    //Setup app
                    val app = LightObject(
                        ip = lightIP,
                        displayName = lightDisplayName
                    )

                    allSavedLights.add(app)
                }
                //Skip corrupt apps
                catch (e: Exception) {
                    Log.d("ERROR", e.toString())
                }
            }
        }

        return allSavedLights
    }

    @Composable
    fun WidgetComposable(lights: List<LightObject>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(
                    RoundedCornerShape(
                        16.dp,
                    )
                )
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lights) { light ->
                WidgetLightItemComposable(lightObject = light, ::onSelectLight)
            }
        }
    }

    fun onSelectLight(lightObject: LightObject) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        val glanceId = GlanceAppWidgetManager(this)
            .getGlanceIdBy(appWidgetId)

        val widgetSharedPref = getSharedPreferences("widget_light_saved", MODE_PRIVATE) ?: return

        val json = JSONObject().apply {
            put("ip", lightObject.ip)
            put("display_name", lightObject.displayName)
        }

        widgetSharedPref.edit()
        {
            putString("selected_light", json.toString())
        }

        lifecycleScope.launch {
            AppWidget().update(this@AppWidgetConfigurationActivity, glanceId)
        }

        setResult(
            RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}