package com.sap.codelab.utils.permission

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PermissionUtilsTest {

    private val context: Context = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { context.getSharedPreferences("permission_requested", Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
    }

    @Test
    fun `hasBeenRequested returns false when permission was never requested`() {
        every { sharedPrefs.getBoolean(Manifest.permission.POST_NOTIFICATIONS, false) } returns false
        assertFalse(PermissionUtils.hasBeenRequested(context, Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `hasBeenRequested returns true after the permission has been marked as requested`() {
        every { sharedPrefs.getBoolean(Manifest.permission.POST_NOTIFICATIONS, false) } returns true
        assertTrue(PermissionUtils.hasBeenRequested(context, Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun `markRequested persists the permission key to SharedPreferences`() {
        PermissionUtils.markRequested(context, Manifest.permission.ACCESS_FINE_LOCATION)
        verify { editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true) }
        verify { editor.apply() }
    }

    @Test
    fun `hasBeenRequested uses the correct permission key — different permissions tracked independently`() {
        every { sharedPrefs.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false) } returns true
        every { sharedPrefs.getBoolean(Manifest.permission.POST_NOTIFICATIONS, false) } returns false

        assertTrue(PermissionUtils.hasBeenRequested(context, Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse(PermissionUtils.hasBeenRequested(context, Manifest.permission.POST_NOTIFICATIONS))
    }
}