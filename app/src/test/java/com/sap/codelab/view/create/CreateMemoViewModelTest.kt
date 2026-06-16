package com.sap.codelab.view.create

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CreateMemoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateMemoViewModel(mockk<Application>(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- isMemoValid ---

    @Test
    fun `isMemoValid is false before any input`() {
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false with title and description but no location`() {
        viewModel.updateMemo("Shopping list", "Milk, eggs, bread")
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false with location but no title or description`() {
        viewModel.setLocation(48.8566, 2.3522)
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is true when title, description and location are all set`() {
        viewModel.updateMemo("Shopping list", "Milk, eggs, bread")
        viewModel.setLocation(48.8566, 2.3522)
        assertTrue(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false when title is blank whitespace`() {
        viewModel.updateMemo("   ", "Milk, eggs, bread")
        viewModel.setLocation(48.8566, 2.3522)
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false when description is blank whitespace`() {
        viewModel.updateMemo("Shopping list", "   ")
        viewModel.setLocation(48.8566, 2.3522)
        assertFalse(viewModel.isMemoValid())
    }

    // --- hasTitleError ---

    @Test
    fun `hasTitleError is true when title is empty`() {
        viewModel.updateMemo("", "Some description")
        assertTrue(viewModel.hasTitleError())
    }

    @Test
    fun `hasTitleError is true when title is blank whitespace`() {
        viewModel.updateMemo("   ", "Some description")
        assertTrue(viewModel.hasTitleError())
    }

    @Test
    fun `hasTitleError is false when title has content`() {
        viewModel.updateMemo("My title", "Some description")
        assertFalse(viewModel.hasTitleError())
    }

    // --- hasTextError ---

    @Test
    fun `hasTextError is true when description is empty`() {
        viewModel.updateMemo("My title", "")
        assertTrue(viewModel.hasTextError())
    }

    @Test
    fun `hasTextError is true when description is blank whitespace`() {
        viewModel.updateMemo("My title", "   ")
        assertTrue(viewModel.hasTextError())
    }

    @Test
    fun `hasTextError is false when description has content`() {
        viewModel.updateMemo("My title", "Some description")
        assertFalse(viewModel.hasTextError())
    }

    // --- hasLocationError / hasLocation ---

    @Test
    fun `hasLocationError is true before any location is set`() {
        assertTrue(viewModel.hasLocationError())
    }

    @Test
    fun `hasLocationError is false after a valid location is set`() {
        viewModel.setLocation(48.8566, 2.3522)
        assertFalse(viewModel.hasLocationError())
    }

    @Test
    fun `hasLocation is false when both coordinates are zero`() {
        viewModel.setLocation(0.0, 0.0)
        assertFalse(viewModel.hasLocation())
    }

    @Test
    fun `hasLocation is true when only latitude is non-zero`() {
        viewModel.setLocation(48.8566, 0.0)
        assertTrue(viewModel.hasLocation())
    }

    @Test
    fun `hasLocation is true when only longitude is non-zero`() {
        viewModel.setLocation(0.0, 2.3522)
        assertTrue(viewModel.hasLocation())
    }
}