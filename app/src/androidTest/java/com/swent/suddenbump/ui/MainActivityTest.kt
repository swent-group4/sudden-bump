package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.utils.isUsingMockViewModel
import com.swent.suddenbump.ui.utils.testableMeetingViewModel
import com.swent.suddenbump.ui.utils.testableUserViewModel
import io.mockk.invoke
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  private lateinit var mockUserViewModel: UserViewModel
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockChatRepository: ChatRepository
  private lateinit var mockMeetingViewModel: MeetingViewModel
  private lateinit var mockMeetingRepository: MeetingRepository

  @get:Rule
  val composeTestRule =
      createAndroidComposeRule<MainActivity>().apply {
        mockChatRepository = mock(ChatRepository::class.java)
        mockUserRepository = mock(UserRepository::class.java)
        mockUserViewModel = UserViewModel(mockUserRepository, mockChatRepository)
        mockMeetingRepository = mock(MeetingRepository::class.java)
        mockMeetingViewModel = MeetingViewModel(mockMeetingRepository)

        isUsingMockViewModel = true
        testableMeetingViewModel = mockMeetingViewModel
        testableUserViewModel = mockUserViewModel
      }

  @Before
  fun setUp() {
    // Define mock user and friend
    val userLocation =
        Location("provider").apply {
          latitude = 37.7749 // San Francisco
          longitude = -122.4194
        }
    val friendLocation =
        Location("provider").apply {
          latitude = 37.7849 // Nearby in San Francisco
          longitude = -122.4094
        }

    val user =
        User(
            uid = "user1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = MutableStateFlow(userLocation))

    val friend =
        User(
            uid = "1",
            firstName = "Friend",
            lastName = "User",
            phoneNumber = "0987654321",
            profilePicture = null,
            emailAddress = "friend.user@example.com",
            lastKnownLocation = MutableStateFlow(friendLocation))

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getUserFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getSharedByFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getRecommendedFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getUserFriendRequests(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getSentFriendRequests(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getBlockedFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(friend))
        }
        .`when`(mockUserRepository)
        .getSharedWithFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(User) -> Unit>(0)
          onSuccess(user)
        }
        .`when`(mockUserRepository)
        .getUserAccount(any(), any())

    // Trigger initialization of the UserViewModel
    mockUserViewModel.setCurrentUser()
  }

  @Test
  fun updateLocationIsCalled() {
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2 : Verifies that updateLocation is called
    composeTestRule.waitForIdle()
    composeTestRule.activity.locationGetter.listener.onLocationResult(
        Location("dummy").apply {
          longitude = -35.00
          latitude = -12.12
        })
    verify(mockUserRepository).updateUserLocation(any(), any(), any(), any())
  }

  @Test
  fun newLocationValueIsUpdated() {
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2 : Verifies that updateLocation is called
    composeTestRule.waitForIdle()
    composeTestRule.activity.locationGetter.listener.onLocationResult(
        Location("dummy").apply {
          longitude = -35.00
          latitude = -12.12
        })

    runTest {
      val emittedValues = composeTestRule.activity.newLocation.take(1).toList()

      assertEquals(listOf(-12.12 to -35.00), emittedValues)
    }
  }
}
