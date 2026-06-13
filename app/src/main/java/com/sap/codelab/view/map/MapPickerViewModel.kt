package com.sap.codelab.view.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.utils.location.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class MapPickerViewModel(application: Application) : AndroidViewModel(application) {

    var selectedLatitude: Double? = null
        private set
    var selectedLongitude: Double? = null
        private set

    private val _address: MutableStateFlow<String?> = MutableStateFlow(null)
    val address: StateFlow<String?> = _address

    fun selectLocation(latitude: Double, longitude: Double) {
        selectedLatitude = latitude
        selectedLongitude = longitude
        _address.value = null
        viewModelScope.launch {
            _address.value = LocationUtils.getAddressFromCoordinates(
                getApplication(), latitude, longitude
            )
        }
    }

    fun hasLocation(): Boolean = selectedLatitude != null && selectedLongitude != null
}