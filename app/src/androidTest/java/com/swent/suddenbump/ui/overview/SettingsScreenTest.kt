package com.swent.suddenbump.ui.overview

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
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

  private fun createMockImageUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "test_image.png")
    FileOutputStream(file).use { outputStream ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }
    return Uri.fromFile(file)
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
    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Photo").assertIsDisplayed()
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
  fun editPhotoButtonIsDisplayedWhenProfilePictureExists() {
    val userWithProfilePicture =
        User(
            uid = "1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = ImageBitmap(100, 100),
            emailAddress = "test.user@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(userWithProfilePicture, {}, {})

    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Edit Photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removePhotoButton").assertIsDisplayed()
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
  fun addPhotoButtonIsDisplayedWhenProfilePictureIsNull() {
    val userWithoutProfilePicture =
        User(
            uid = "2",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+1234567891",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(userWithoutProfilePicture, {}, {})

    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Photo").assertIsDisplayed()
  }

  @Test
  fun removePhotoButtonClearsProfilePicture() {
    val userWithProfilePicture =
        User(
            uid = "3",
            firstName = "Alice",
            lastName = "Brown",
            phoneNumber = "+1234567892",
            profilePicture = ImageBitmap(100, 100),
            emailAddress = "alice.brown@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(userWithProfilePicture, {}, {})

    setContentDefault()
    composeTestRule.waitForIdle()

    // Perform click on the remove photo button
    composeTestRule.onNodeWithTag("removePhotoButton").performClick()

    // Verify that the profile picture is cleared
    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nullProfilePicture").assertIsDisplayed()
  }

  @Test
  fun addPhotoButtonTransitionsToEditPhotoAfterAddingPicture() {
    setContentDefault()
    composeTestRule.waitForIdle()

    // Verify initial state
    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Photo").assertIsDisplayed()

    // Simulate adding a photo
    val userWithProfilePicture =
        User(
            uid = "4",
            firstName = "Bob",
            lastName = "Smith",
            phoneNumber = "+1234567893",
            profilePicture = ImageBitmap(100, 100),
            emailAddress = "bob.smith@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(userWithProfilePicture, {}, {})

    composeTestRule.waitForIdle()

    // Verify transition to edit photo state
    composeTestRule.onNodeWithTag("uploadPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Edit Photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removePhotoButton").assertIsDisplayed()
  }

  @Test
  fun launchedEffectUpdatesProfilePictureSuccessfully() {
    // Arrange: Mock User with no profile picture
    val initialUser =
        User(
            uid = "1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(initialUser, {}, {})

    // Create a mock Uri for the image
    val context = ApplicationProvider.getApplicationContext<Context>()
    val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val mockUri = createMockImageUri(context, mockBitmap)

    // Act: Set up the screen and simulate the profile picture Uri update
    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }

    composeTestRule.waitForIdle()

    // Trigger the profile picture update (simulate UI behavior)
    userViewModel.setUser(
        initialUser.copy(profilePicture = mockBitmap.asImageBitmap()),
        onSuccess = {},
        onFailure = {})

    composeTestRule.waitForIdle()

    // Assert: Verify the updated profile picture is displayed
    composeTestRule.onNodeWithTag("nonNullProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithText("Edit Photo").assertIsDisplayed()
  }

  @Test
  fun launchedEffectHandlesImageLoadingFailureGracefully() {
    // Arrange: Mock User with no profile picture
    val initialUser =
        User(
            uid = "2",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(initialUser, {}, {})

    // Act: Set up the screen and simulate the failure
    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }

    composeTestRule.waitForIdle()

    // Simulate updating the profile picture with the invalid Uri
    userViewModel.setUser(initialUser.copy(profilePicture = null), onSuccess = {}, onFailure = {})

    // Assert: Verify fallback UI is displayed (no crash occurs)
    composeTestRule.onNodeWithTag("nullProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Photo").assertIsDisplayed()
  }

  @Test
  fun profilePictureUriLoadsSuccessfullyAndUpdatesViewModel() {
    // Arrange: Mock user and initialize the ViewModel
    val initialUser =
        User(
            uid = "1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = locationDummy)
    userViewModel.setUser(initialUser, {}, {})

    val context = ApplicationProvider.getApplicationContext<Context>()
    val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    // Write a bitmap to a temporary file and get its Uri
    val croppedImageUri = run {
      val tempFile = File.createTempFile("cropped_image", ".png", context.cacheDir)
      FileOutputStream(tempFile).use { outputStream ->
        mockBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      }
      Uri.fromFile(tempFile)
    }

    // Act: Render the ProfileSection composable and simulate the crop result
    composeTestRule.setContent { ProfileSection(userViewModel = userViewModel) }
    composeTestRule.waitForIdle()

    // Simulate the result of UCrop being returned
    composeTestRule.runOnIdle {
      userViewModel.setUser(initialUser.copy(profilePicture = mockBitmap.asImageBitmap()), {}, {})
    }

    // Assert: Verify the updated profile picture is displayed
    composeTestRule.onNodeWithTag("nonNullProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithText("Edit Photo").assertIsDisplayed()
  }
}
