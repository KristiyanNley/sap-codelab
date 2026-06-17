package com.sap.codelab.model

internal data class NearbyPlace(
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int
)