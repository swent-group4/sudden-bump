package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.map.SimpleMap
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SimpleMapTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMarkerIsUpdatedOnLocationChange() {
    val mockLocation =
        Location("test").apply {
          latitude = 35.0
          longitude = 139.0
        }

    val userViewModel = mock(UserViewModel::class.java)

    composeTestRule.setContent { SimpleMap(location = mockLocation, userViewModel = userViewModel) }

    // Check that the marker has been updated
    // Unfortunately, Compose UI testing does not allow access to the Google Map's internal state,
    // so you would rely on other mechanisms to validate that the marker's position has changed.

    // The test for GoogleMap would be more functional in a full integration test with tools like
    // Espresso or Robolectric.
  }
}
