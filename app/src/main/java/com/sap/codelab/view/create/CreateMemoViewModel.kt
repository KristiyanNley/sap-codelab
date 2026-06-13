package com.sap.codelab.view.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sap.codelab.location.GeofenceManager
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.utils.extensions.empty
import kotlinx.coroutines.launch

internal class CreateMemoViewModel(application: Application) : AndroidViewModel(application) {

    private var title: String = String.empty()
    private var description: String = String.empty()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    fun setLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
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

    fun isMemoValid(): Boolean = title.isNotBlank() && description.isNotBlank()

    fun hasTextError(): Boolean = description.isBlank()

    fun hasTitleError(): Boolean = title.isBlank()
}