package com.sap.codelab.view.detail

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.location.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val LOCATION_UPDATE_INTERVAL_MS = 2_000L
private const val LOCATION_MIN_UPDATE_DISTANCE_METERS = 1f

internal class ViewMemoViewModel(application: Application) : AndroidViewModel(application) {

    private val _memo: MutableStateFlow<Memo?> = MutableStateFlow(null)
    val memo: StateFlow<Memo?> = _memo

    private val _address: MutableStateFlow<String?> = MutableStateFlow(null)
    val address: StateFlow<String?> = _address

    private val _distanceMeters: MutableStateFlow<Int?> = MutableStateFlow(null)
    val distanceMeters: StateFlow<Int?> = _distanceMeters

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val memo = _memo.value ?: return
            if (memo.reminderLatitude == 0.0 && memo.reminderLongitude == 0.0) return
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                memo.reminderLatitude, memo.reminderLongitude,
                results
            )
            _distanceMeters.value = results[0].toInt()
        }
    }

    fun loadMemo(memoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val memo = Repository.getMemoById(memoId)
            _memo.value = memo
            if (memo.reminderLatitude != 0.0 || memo.reminderLongitude != 0.0) {
                launch {
                    _address.value = LocationUtils.getAddressFromCoordinates(
                        getApplication(), memo.reminderLatitude, memo.reminderLongitude
                    )
                }
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(LOCATION_MIN_UPDATE_DISTANCE_METERS)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}