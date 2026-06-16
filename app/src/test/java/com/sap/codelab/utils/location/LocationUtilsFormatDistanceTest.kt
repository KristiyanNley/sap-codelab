package com.sap.codelab.utils.location

import android.content.Context
import com.sap.codelab.R
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LocationUtilsFormatDistanceTest {

    // relaxed = true: getString returns "" by default so no every{} boilerplate is needed
    private val context: Context = mockk(relaxed = true)

    @Test
    fun `values below 1000m use the metres string resource`() {
        LocationUtils.formatDistance(context, 0)
        LocationUtils.formatDistance(context, 999)
        verify(atLeast = 1) { context.getString(R.string.distance_meters, any()) }
        verify(exactly = 0) { context.getString(R.string.distance_kilometers, any()) }
    }

    @Test
    fun `999m is the last value to use the metres resource`() {
        LocationUtils.formatDistance(context, 999)
        verify(exactly = 1) { context.getString(R.string.distance_meters, any()) }
        verify(exactly = 0) { context.getString(R.string.distance_kilometers, any()) }
    }

    @Test
    fun `1000m is the first value to switch to the kilometres resource`() {
        LocationUtils.formatDistance(context, 1000)
        verify(exactly = 0) { context.getString(R.string.distance_meters, any()) }
        verify(exactly = 1) { context.getString(R.string.distance_kilometers, any()) }
    }

    @Test
    fun `values from 1000m upward use the kilometres string resource`() {
        LocationUtils.formatDistance(context, 1000)
        LocationUtils.formatDistance(context, 5000)
        verify(exactly = 0) { context.getString(R.string.distance_meters, any()) }
        verify(atLeast = 1) { context.getString(R.string.distance_kilometers, any()) }
    }
}