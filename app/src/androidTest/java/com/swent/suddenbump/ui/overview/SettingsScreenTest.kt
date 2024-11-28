package com.swent.suddenbump.ui.overview

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
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
  }

  private fun setContentDefault() {
    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          onNotificationsEnabledChange = { notificationsEnabled = it })
    }
  }

  @Test
  fun hasRequiredComponents() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).goBack()
  }

  @Test
  fun accountButtonNavigatesToAccountScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Account").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.ACCOUNT)
  }

  @Test
  fun confidentialityButtonNavigatesToConfidentialityScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Confidentiality").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.CONFIDENTIALITY)
  }

  @Test
  fun discussionsButtonNavigatesToDiscussionsScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Discussions").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.DISCUSSIONS)
  }

  @Test
  fun storageAndDataButtonNavigatesToStorageAndDataScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Scroll down to bring the "Storage and Data" option into view
    composeTestRule
        .onNodeWithTag("settingsLazyColumn")
        .performScrollToNode(hasTestTag("StorageAndDataOption"))

    // Now check if the node exists and perform the action
    composeTestRule
        .onNodeWithTag("StorageAndDataOption")
        .assertExists(
            "Storage and Data option not found. Ensure the tag is correctly set in the composable.")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle() // Wait for UI to settle
    verify(navigationActions).navigateTo(screen = Screen.STORAGE_AND_DATA)
  }

  @Test
  fun helpButtonNavigatesToHelpScreen() {
    setContentDefault()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Scroll to bring the "Help" option into view
    composeTestRule
        .onNodeWithTag("settingsLazyColumn")
        .performScrollToNode(hasTestTag("HelpOption")) // Use testTag to scroll to the Help button

    // Now check if the "Help" button exists, is displayed, and perform the click
    composeTestRule
        .onNodeWithTag("HelpOption")
        .assertExists("Help button does not exist.")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.HELP)
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
          onNotificationsEnabledChange = { notificationsEnabled = it },
          uri = uriImage)
    }
    composeTestRule.waitForIdle()

    verify(userRepository).updateUserAccount(any(), any(), any())
  }
}

object Screen {
  const val ACCOUNT = "AccountScreen"
  const val CONFIDENTIALITY = "ConfidentialityScreen"
  const val DISCUSSIONS = "DiscussionsScreen"
  const val STORAGE_AND_DATA = "StorageAndDataScreen"
  const val HELP = "HelpScreen"
}
