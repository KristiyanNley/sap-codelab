package com.sap.codelab.view.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.location.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ViewMemoViewModel(application: Application) : AndroidViewModel(application) {

    private val _memo: MutableStateFlow<Memo?> = MutableStateFlow(null)
    val memo: StateFlow<Memo?> = _memo

    private val _address: MutableStateFlow<String?> = MutableStateFlow(null)
    val address: StateFlow<String?> = _address

    private val _distanceMeters: MutableStateFlow<Int?> = MutableStateFlow(null)
    val distanceMeters: StateFlow<Int?> = _distanceMeters

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
                launch {
                    _distanceMeters.value = LocationUtils.getDistanceMeters(
                        getApplication(), memo.reminderLatitude, memo.reminderLongitude
                    )
                }
            }
        }
    }
}