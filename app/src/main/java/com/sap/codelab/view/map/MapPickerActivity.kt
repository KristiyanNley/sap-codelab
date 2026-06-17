package com.sap.codelab.view.map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import androidx.core.view.WindowCompat
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityMapPickerBinding
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

internal const val EXTRA_LATITUDE = "extra_latitude"
internal const val EXTRA_LONGITUDE = "extra_longitude"

private const val MAP_DEFAULT_ZOOM = 14.0
private const val MAP_SEARCH_RESULT_ZOOM = 15.0
private const val FALLBACK_LATITUDE_PARIS = 48.8566
private const val FALLBACK_LONGITUDE_PARIS = 2.3522

internal class MapPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var model: MapPickerViewModel
    private lateinit var suggestionAdapter: SuggestionAdapter
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        model = ViewModelProvider(this)[MapPickerViewModel::class.java]
        setupMap()
        setupSearch()
        observeViewModel()
    }

    @SuppressLint("MissingPermission")
    private fun centerOnCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    binding.mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                }
            }
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(MAP_DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(FALLBACK_LATITUDE_PARIS, FALLBACK_LONGITUDE_PARIS))
        }
        centerOnCurrentLocation()

        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                placeMarker(point)
                model.selectLocation(point.latitude, point.longitude)
                model.clearSuggestions()
                showAddressLoading()
                hideKeyboard()
                return true
            }
            override fun longPressHelper(point: GeoPoint): Boolean = false
        })
        binding.mapView.overlays.add(0, tapOverlay)
    }

    private fun setupSearch() {
        suggestionAdapter = SuggestionAdapter { place ->
            binding.searchInput.setText(place.displayName)
            binding.searchInput.setSelection(place.displayName.length)
            model.onSuggestionSelected(place)
            hideKeyboard()
        }

        binding.suggestionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MapPickerActivity)
            adapter = suggestionAdapter
            addItemDecoration(DividerItemDecoration(this@MapPickerActivity, LinearLayoutManager.VERTICAL))
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                model.onQueryChanged(s?.toString().orEmpty())
            }
        })

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                model.searchLocation(binding.searchInput.text?.toString().orEmpty())
                hideKeyboard()
                true
            } else false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            model.suggestions.collect { suggestions ->
                val hasSuggestions = suggestions.isNotEmpty()
                suggestionAdapter.submitList(suggestions)
                binding.suggestionsRecycler.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
                binding.suggestionsDivider.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
            }
        }
        lifecycleScope.launch {
            model.address.collect { address ->
                if (address != null) {
                    binding.addressCard.visibility = View.VISIBLE
                    binding.addressPreviewText.text = address
                }
            }
        }
        lifecycleScope.launch {
            model.searchResult.collect { result ->
                when (result) {
                    is SearchResult.Found -> {
                        val point = GeoPoint(result.lat, result.lng)
                        placeMarker(point)
                        binding.mapView.controller.setZoom(MAP_SEARCH_RESULT_ZOOM)
                        binding.mapView.controller.animateTo(point)
                        showAddressLoading()
                    }
                    is SearchResult.NotFound ->
                        Toast.makeText(this@MapPickerActivity, R.string.location_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddressLoading() {
        binding.addressCard.visibility = View.VISIBLE
        binding.addressPreviewText.text = getString(R.string.loading_address)
    }

    private fun placeMarker(point: GeoPoint) {
        marker?.let { binding.mapView.overlays.remove(it) }
        marker = Marker(binding.mapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = getString(R.string.selected_location)
        }
        binding.mapView.overlays.add(marker)
        binding.mapView.invalidate()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.action_confirm_location -> {
            if (model.hasLocation()) {
                val result = Intent().apply {
                    putExtra(EXTRA_LATITUDE, model.selectedLatitude!!)
                    putExtra(EXTRA_LONGITUDE, model.selectedLongitude!!)
                }
                setResult(RESULT_OK, result)
                finish()
            } else {
                Toast.makeText(this, R.string.tap_map_to_select, Toast.LENGTH_SHORT).show()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}