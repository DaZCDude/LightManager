package com.dazcdude.wiz

data class LightData(
    val ip: String,
    val state: Boolean,
    val dimming: Int,
    val colorMode: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val colorTemp: Int
)