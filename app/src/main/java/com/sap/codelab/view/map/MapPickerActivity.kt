package com.sap.codelab.view.map

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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

internal class MapPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var model: MapPickerViewModel
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        model = ViewModelProvider(this)[MapPickerViewModel::class.java]
        setupMap()
        observeAddress()
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(48.8566, 2.3522))
        }

        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                placeMarker(point)
                model.selectLocation(point.latitude, point.longitude)
                showAddressLoading()
                return true
            }
            override fun longPressHelper(point: GeoPoint): Boolean = false
        })
        binding.mapView.overlays.add(0, tapOverlay)
    }

    private fun observeAddress() {
        lifecycleScope.launch {
            model.address.collect { address ->
                if (address != null) {
                    binding.addressCard.visibility = View.VISIBLE
                    binding.addressPreviewText.text = address
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