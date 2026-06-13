package com.sap.codelab.view.map

import androidx.lifecycle.ViewModel

internal class MapPickerViewModel : ViewModel() {

    var selectedLatitude: Double? = null
        private set
    var selectedLongitude: Double? = null
        private set

    fun selectLocation(latitude: Double, longitude: Double) {
        selectedLatitude = latitude
        selectedLongitude = longitude
    }

    fun hasLocation(): Boolean = selectedLatitude != null && selectedLongitude != null
}