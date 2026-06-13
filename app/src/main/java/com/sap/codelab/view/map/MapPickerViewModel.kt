package com.sap.codelab.view.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.utils.location.LocationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal sealed class SearchResult {
    data class Found(val lat: Double, val lng: Double) : SearchResult()
    object NotFound : SearchResult()
}

internal class MapPickerViewModel(application: Application) : AndroidViewModel(application) {

    var selectedLatitude: Double? = null
        private set
    var selectedLongitude: Double? = null
        private set

    private val _address: MutableStateFlow<String?> = MutableStateFlow(null)
    val address: StateFlow<String?> = _address

    private val _searchResult = MutableSharedFlow<SearchResult>()
    val searchResult: SharedFlow<SearchResult> = _searchResult

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

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            val result = LocationUtils.searchLocation(getApplication(), query)
            if (result != null) {
                selectLocation(result.first, result.second)
                _searchResult.emit(SearchResult.Found(result.first, result.second))
            } else {
                _searchResult.emit(SearchResult.NotFound)
            }
        }
    }

    fun hasLocation(): Boolean = selectedLatitude != null && selectedLongitude != null
}