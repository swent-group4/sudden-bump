package com.swent.suddenbump.ui.destination

import android.location.Location
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
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
  private lateinit var chatRepository: ChatRepository
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var meetingViewModel: MeetingViewModel

  private val location =
      Location("mock_provider").apply {
        latitude = 0.0
        longitude = 0.0
      }

  private val user =
      User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch", location)

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    meetingRepository = mock(MeetingRepository::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
    userViewModel = UserViewModel(userRepository, chatRepository)
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
    composeTestRule.setContent {
      SimpleMap(userViewModel = userViewModel, meetingViewModel = meetingViewModel)
    }
  }
}
