package com.sap.codelab.view.create

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sap.codelab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CreateMemoValidationTest {

    @get:Rule(order = 0)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(CreateMemo::class.java)

    @Test
    fun savingWithNoInput_showsTitleError() {
        onView(withId(R.id.action_save)).perform(click())
        onView(withText(R.string.memo_title_empty_error)).check(matches(isDisplayed()))
    }

    @Test
    fun savingWithTitleOnly_showsDescriptionError() {
        onView(withId(R.id.memo_title))
            .perform(typeText("My Title"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())
        onView(withText(R.string.memo_text_empty_error)).check(matches(isDisplayed()))
    }

    @Test
    fun savingWithTitleAndDescriptionButNoLocation_showsLocationSnackbar() {
        onView(withId(R.id.memo_title))
            .perform(typeText("My Title"), closeSoftKeyboard())
        onView(withId(R.id.memo_description))
            .perform(typeText("My description"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())
        onView(withText(R.string.memo_location_empty_error)).check(matches(isDisplayed()))
    }
}