package com.sap.codelab.view.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.model.Memo
import com.sap.codelab.utils.location.LocationUtils
import kotlinx.coroutines.launch

internal const val BUNDLE_MEMO_ID: String = "memoId"

internal class ViewMemo : AppCompatActivity() {

    private lateinit var binding: ActivityViewMemoBinding
    private lateinit var model: ViewMemoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        model = ViewModelProvider(this)[ViewMemoViewModel::class.java]

        if (savedInstanceState == null) {
            lifecycleScope.launch { model.memo.collect { memo -> memo?.let { bindMemo(it) } } }
            lifecycleScope.launch { model.address.collect { it?.let { addr -> showAddress(addr) } } }
            lifecycleScope.launch { model.distanceMeters.collect { it?.let { d -> showDistance(d) } } }
            model.loadMemo(intent.getLongExtra(BUNDLE_MEMO_ID, -1))
        }
    }

    private fun bindMemo(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false

            locationCard.isClickable = false
            locationCard.isFocusable = false
            pickLocationChevron.visibility = View.GONE

            val hasLocation = memo.reminderLatitude != 0.0 || memo.reminderLongitude != 0.0
            if (hasLocation) {
                val primaryColor = ContextCompat.getColor(this@ViewMemo, R.color.colorPrimary)
                val primaryList = ColorStateList.valueOf(primaryColor)
                locationIcon.imageTintList = primaryList
                locationCard.strokeColor = primaryColor
                locationActionLabel.setText(R.string.location_label)
                locationStatusText.text = getString(R.string.loading_address)
                distanceText.visibility = View.VISIBLE
                distanceText.text = getString(R.string.calculating_distance)
            } else {
                locationCard.visibility = View.GONE
            }
        }
    }

    private fun showAddress(address: String) {
        binding.contentCreateMemo.locationStatusText.text = address
    }

    private fun showDistance(meters: Int) {
        binding.contentCreateMemo.distanceText.text = LocationUtils.formatDistance(meters)
    }
}