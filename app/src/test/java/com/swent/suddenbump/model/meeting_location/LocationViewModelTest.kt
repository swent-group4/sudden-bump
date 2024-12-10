package com.swent.suddenbump.model.meeting_location

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationViewModelTest {

    @Mock private lateinit var mockRepository: LocationRepository

    private lateinit var viewModel: LocationViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = LocationViewModel(mockRepository)
    }

    @Test
    fun `setQuery should update query state`() {
        val testQuery = "test"

        viewModel.setQuery(testQuery)

        assertEquals(testQuery, viewModel.query.value)
    }

    @Test
    fun `setQuery should trigger search and update location suggestions`() = runBlocking {
        val testQuery = "location"
        val mockLocations = listOf(
            Location( 12.34, 56.78, "Location 1"),
            Location( 98.76, 54.32, "Location 2")
        )

        doAnswer { invocation ->
            val onSuccess = invocation.arguments[1] as (List<Location>) -> Unit
            onSuccess(mockLocations)
            null
        }.`when`(mockRepository).search(org.mockito.kotlin.eq(testQuery), org.mockito.kotlin.any(), org.mockito.kotlin.any())

        viewModel.setQuery(testQuery)

        assertEquals(mockLocations, viewModel.locationSuggestions.value)
    }

    @Test
    fun `setQuery should clear location suggestions for blank query`() = runBlocking {
        viewModel.setQuery("")

        assertTrue(viewModel.locationSuggestions.value.isEmpty())
    }

    @Test
    fun `searchLocations should handle repository failure`() = runBlocking {
        val testQuery = "location"
        val exception = Exception("Search failed")

        doAnswer { invocation ->
            val onFailure = invocation.arguments[2] as (Exception) -> Unit
            onFailure(exception)
            null
        }.`when`(mockRepository).search(org.mockito.kotlin.eq(testQuery), org.mockito.kotlin.any(), org.mockito.kotlin.any())

        viewModel.setQuery(testQuery)

        // Ensure no crash or improper state update
        assertTrue(viewModel.locationSuggestions.value.isEmpty())
    }
}
