package com.sap.codelab.view.create

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.utils.permission.PermissionUtils
import com.sap.codelab.view.map.EXTRA_LATITUDE
import com.sap.codelab.view.map.EXTRA_LONGITUDE
import com.sap.codelab.view.map.MapPickerActivity
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private var menuItemSave: MenuItem? = null
    private var pendingMapOpen = false

    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra(EXTRA_LATITUDE, 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra(EXTRA_LONGITUDE, 0.0) ?: 0.0
            binding.contentCreateMemo.locationStatusText.text = getString(R.string.loading_address)
            model.setLocation(lat, lng)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                requestBackgroundLocationIfNeeded()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Denied once, can still ask again — offer retry
                Snackbar.make(binding.root, R.string.location_permission_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { requestLocationPermissionAndOpenMap() }
                    .show()
            }
            else -> {
                // shouldShowRationale is false after requesting = permanently denied
                showLocationPermissionDenied()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]

        binding.locationPermissionDenied.openSettingsButton.setOnClickListener {
            PermissionUtils.openAppSettings(this)
        }

        lifecycleScope.launch {
            model.locationDisplay.collect { address ->
                updateLocationCard(address)
            }
        }

        binding.contentCreateMemo.triggerDistanceText.visibility = View.GONE

        binding.contentCreateMemo.locationCard.setOnClickListener {
            requestLocationPermissionAndOpenMap()
        }

        binding.contentCreateMemo.nearbyPlacesCompose.setContent {
            val state by model.nearbyPlacesState.collectAsState()
            NearbyPlacesSection(
                state = state,
                onRequestLoad = { model.loadNearbyPlaces() },
                onPlaceSelected = { place ->
                    binding.contentCreateMemo.locationStatusText.text = getString(R.string.loading_address)
                    model.setLocation(place.latitude, place.longitude)
                }
            )
        }

    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
            binding.locationPermissionDenied.root.isVisible
        ) {
            showContent()
        }
        if (pendingMapOpen) {
            pendingMapOpen = false
            openMapPicker()
        }
    }

    private fun requestLocationPermissionAndOpenMap() {
        when {
            PermissionUtils.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestBackgroundLocationIfNeeded()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Denied once — show rationale before asking again
                showLocationRationaleDialog()
            }
            else -> {
                // First time OR permanently denied — let the system decide.
                // If permanently denied the callback fires immediately with granted=false
                // and shouldShowRationale=false, which we detect there.
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !PermissionUtils.isGranted(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            showBackgroundLocationRationaleDialog()
        } else {
            openMapPicker()
        }
    }

    private fun showBackgroundLocationRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.background_location_rationale_title)
            .setMessage(R.string.background_location_rationale_message)
            .setPositiveButton(R.string.allow) { _, _ ->
                pendingMapOpen = true
                PermissionUtils.openAppSettings(this)
            }
            .setNegativeButton(R.string.dont_allow) { _, _ ->
                openMapPicker()
            }
            .show()
    }

    private fun showLocationRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.location_permission_rationale_title)
            .setMessage(R.string.location_permission_rationale_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.not_now, null)
            .show()
    }

    private fun showLocationPermissionDenied() {
        binding.contentCreateMemo.root.visibility = View.GONE
        binding.locationPermissionDenied.root.visibility = View.VISIBLE
        menuItemSave?.isVisible = false
    }

    private fun showContent() {
        binding.locationPermissionDenied.root.visibility = View.GONE
        binding.contentCreateMemo.root.visibility = View.VISIBLE
        menuItemSave?.isVisible = true
    }

    private fun openMapPicker() {
        mapPickerLauncher.launch(Intent(this, MapPickerActivity::class.java))
    }

    private fun updateLocationCard(address: String?) {
        binding.contentCreateMemo.run {
            if (address == null) {
                locationActionLabel.setText(R.string.add_location)
                locationStatusText.text = getString(R.string.no_location_selected)
                val secondaryColor = ContextCompat.getColor(this@CreateMemo, android.R.color.darker_gray)
                val secondaryList = ColorStateList.valueOf(secondaryColor)
                locationIcon.imageTintList = secondaryList
                pickLocationChevron.imageTintList = secondaryList
                locationCard.strokeColor = secondaryColor
            } else {
                locationActionLabel.setText(R.string.edit_location)
                locationStatusText.text = address
                val primaryColor = ContextCompat.getColor(this@CreateMemo, R.color.colorPrimary)
                val primaryList = ColorStateList.valueOf(primaryColor)
                locationIcon.imageTintList = primaryList
                pickLocationChevron.imageTintList = primaryList
                locationCard.strokeColor = primaryColor
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        menuItemSave = menu.findItem(R.id.action_save)
        menuItemSave?.isVisible = binding.locationPermissionDenied.root.visibility != View.VISIBLE
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveMemo() {
        binding.contentCreateMemo.run {
            model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())
            if (model.isMemoValid()) {
                model.saveMemo()
                setResult(RESULT_OK)
                finish()
            } else {
                memoTitleContainer.error = getErrorMessage(model.hasTitleError(), R.string.memo_title_empty_error)
                memoDescription.error = getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
                if (model.hasLocationError()) {
                    val errorColor = ContextCompat.getColor(this@CreateMemo, com.google.android.material.R.color.design_error)
                    locationCard.strokeColor = errorColor
                    locationIcon.imageTintList = ColorStateList.valueOf(errorColor)
                    pickLocationChevron.imageTintList = ColorStateList.valueOf(errorColor)
                    Snackbar.make(binding.root, R.string.memo_location_empty_error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String? {
        return if (hasError) getString(errorMessageResId) else null
    }
}