package com.sap.codelab.view.create

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.sap.codelab.location.GeofenceManager
import com.sap.codelab.model.Memo
import com.sap.codelab.model.NearbyPlace
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.utils.location.LocationUtils
import com.sap.codelab.utils.location.OverpassService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal sealed interface NearbyPlacesUiState {
    data object Idle : NearbyPlacesUiState
    data object Loading : NearbyPlacesUiState
    data class Success(val places: List<NearbyPlace>) : NearbyPlacesUiState
    data object Error : NearbyPlacesUiState
}

internal class CreateMemoViewModel(application: Application) : AndroidViewModel(application) {

    private var title: String = String.empty()
    private var description: String = String.empty()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val _locationDisplay: MutableStateFlow<String?> = MutableStateFlow(null)
    val locationDisplay: StateFlow<String?> = _locationDisplay

    private val _nearbyPlacesState: MutableStateFlow<NearbyPlacesUiState> = MutableStateFlow(NearbyPlacesUiState.Idle)
    val nearbyPlacesState: StateFlow<NearbyPlacesUiState> = _nearbyPlacesState

    private val _selectedNearbyPlace: MutableStateFlow<NearbyPlace?> = MutableStateFlow(null)
    val selectedNearbyPlace: StateFlow<NearbyPlace?> = _selectedNearbyPlace

    @SuppressLint("MissingPermission")
    fun loadNearbyPlaces() {
        _nearbyPlacesState.value = NearbyPlacesUiState.Loading
        viewModelScope.launch {
            val location = suspendCancellableCoroutine { cont ->
                LocationServices.getFusedLocationProviderClient(getApplication())
                    .lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (location == null) {
                _nearbyPlacesState.value = NearbyPlacesUiState.Error
                return@launch
            }
            val places = OverpassService.getNearbyPlaces(location.latitude, location.longitude)
            _nearbyPlacesState.value = if (places.isNotEmpty()) {
                NearbyPlacesUiState.Success(places)
            } else {
                NearbyPlacesUiState.Error
            }
        }
    }

    fun selectNearbyPlace(place: NearbyPlace) {
        _selectedNearbyPlace.value = place
        setLocation(place.latitude, place.longitude)
    }

    fun setLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
        viewModelScope.launch {
            _locationDisplay.value = LocationUtils.getAddressFromCoordinates(getApplication(), lat, lng)
        }
    }

    fun hasLocation(): Boolean = latitude != 0.0 || longitude != 0.0

    fun saveMemo() {
        val memo = Memo(
            id = 0,
            title = title,
            description = description,
            reminderDate = 0,
            reminderLatitude = latitude,
            reminderLongitude = longitude,
            isDone = false
        )
        ScopeProvider.application.launch {
            val memoId = Repository.saveMemo(memo)
            if (hasLocation()) {
                GeofenceManager.registerGeofence(getApplication(), memoId, latitude, longitude)
            }
        }
    }

    fun updateMemo(newTitle: String, newDescription: String) {
        title = newTitle
        description = newDescription
    }

    fun isMemoValid(): Boolean = title.isNotBlank() && description.isNotBlank() && hasLocation()

    fun hasTextError(): Boolean = description.isBlank()

    fun hasTitleError(): Boolean = title.isBlank()

    fun hasLocationError(): Boolean = !hasLocation()
}