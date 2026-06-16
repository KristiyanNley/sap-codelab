package com.sap.codelab.view.home

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sap.codelab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class HomeScreenTest {

    // Grant upfront so the notification permission dialog never blocks the test
    @get:Rule
    val notificationPermission: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule
    val activityRule = ActivityScenarioRule(Home::class.java)

    @Test
    fun homeScreen_showsToolbarAndFab() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingFab_navigatesToCreateMemoScreen() {
        onView(withId(R.id.fab)).perform(click())
        // memo_title is the title input that only exists in CreateMemo
        onView(withId(R.id.memo_title)).check(matches(isDisplayed()))
    }
}