package com.swent.suddenbump.ui.overview

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import io.mockk.mockk
import java.io.File
import java.io.FileOutputStream
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var meetingViewModel: MeetingViewModel
  private var notificationsEnabled = true

  @get:Rule val composeTestRule = createComposeRule()

  private val locationDummy =
      Location("mock_provider").apply {
        latitude = 46.5180
        longitude = 6.5680
      }

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(userRepository, chatRepository)
    meetingRepository = mockk(relaxed = true)
    meetingViewModel = MeetingViewModel(meetingRepository)
  }

  private fun setContentDefault() {
    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    setContentDefault()
    composeTestRule.waitForIdle()

    // Verify that the settings screen container is displayed
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Verify that the top bar title is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()

    // Verify other required components
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    setContentDefault()
    composeTestRule.waitForIdle()

    // Verify that the back button is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()

    // Perform click on the back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Verify the goBack action is triggered
    verify(navigationActions).goBack()
  }

  @Test
  fun accountButtonNavigatesToAccountScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    // Verify that the Account option navigates to the Account screen
    composeTestRule.onNodeWithTag("AccountOption").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun discussionsButtonNavigatesToDiscussionsScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    // Verify that the Discussions option navigates to the Discussions screen
    composeTestRule.onNodeWithTag("DiscussionsOption").performClick()
    verify(navigationActions).navigateTo("DiscussionsScreen")
  }

  @Test
  fun defaultProfilePictureDisplaysWhenNull() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(testTag = "nullProfilePicture", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun customProfilePictureDisplaysWhenNotNull() {
    setContentDefault()
    composeTestRule.waitForIdle()

    val userWithNullProfilePicture =
        User(
            uid = "3",
            firstName = "Alice",
            lastName = "Brown",
            phoneNumber = "+1234567892",
            profilePicture = ImageBitmap(100, 100),
            emailAddress = "alice.brown@example.com",
            lastKnownLocation = MutableStateFlow(locationDummy))
    userViewModel.setUser(userWithNullProfilePicture, {}, {})

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(testTag = "nonNullProfilePicture", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun chosenProfilePictureDisplaysWhenUri() {
    var testContext: Context = ApplicationProvider.getApplicationContext()

    val uriExternal = testContext.getExternalFilesDir(null)?.toURI()

    val uriImage = Uri.parse("file://${uriExternal!!.path}imagetest.jpeg")

    val fileOutputStream: FileOutputStream
    val bitmap = ImageBitmap(100, 100).asAndroidBitmap()

    try {
      val file = File(uriImage.path!!).createFileIfNeeded()
      fileOutputStream = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream).also {
        fileOutputStream.close()
      }
    } catch (e: Exception) {
      Log.d("Debug", e.toString())
      fail("Couldn't write the file for the test")
    }

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
        }
        .`when`(userRepository)
        .updateUserAccount(any(), any(), any())

    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel,
          uri = uriImage)
    }
    composeTestRule.waitForIdle()

    verify(userRepository).updateUserAccount(any(), any(), any())
  }
}
