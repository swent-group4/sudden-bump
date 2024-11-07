package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.map.SimpleMap
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SimpleMapTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  private val user =
      User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch")

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)
  }

  @Test
  fun testMarkerIsUpdatedOnLocationChange() {
    // Mock the location change
    val newLocation =
        Location("test").apply {
          latitude = 35.0
          longitude = 139.0
        }

    // Render the SimpleMap composable with the initial location
    composeTestRule.setContent { SimpleMap(location = newLocation, userViewModel = userViewModel) }
  }
}
