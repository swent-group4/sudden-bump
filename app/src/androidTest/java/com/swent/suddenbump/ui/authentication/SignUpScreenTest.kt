package com.swent.suddenbump.ui.authentication

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.yalantis.ucrop.UCrop
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class SignUpScreenTest {

  private lateinit var userRepository: UserRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)
  }

  @Test
  fun testSignUpScreen_initialState() {
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }

    // Check if fields are displayed
    composeTestRule.onNodeWithTag("firstNameField").assertExists()
    composeTestRule.onNodeWithTag("lastNameField").assertExists()
    composeTestRule.onNodeWithTag("emailField").assertExists()
    composeTestRule.onNodeWithTag("phoneField").assertExists()
    composeTestRule.onNodeWithTag("createAccountButton").assertExists()

    // Check that the profile picture button is displayed
    composeTestRule.onNodeWithTag("noProfilePic", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testInputFields_interaction() {
    // Set the initial content for testing
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }

    // Test user input in the text fields
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("firstNameField").assertTextContains("John")

    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("lastNameField").assertTextContains("Doe")

    composeTestRule.onNodeWithTag("emailField").performTextInput("john.doe@example.com")
    composeTestRule.onNodeWithTag("emailField").assertTextContains("john.doe@example.com")

    composeTestRule.onNodeWithTag("phoneField").performTextInput("+1234567890")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+1 234-567-890")
  }

  @Test
  fun testPhoneNumberVisualTransformation() {
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+1234567890")

    // Check visual transformation
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+1 234-567-890")
    composeTestRule.onNodeWithTag("phoneField").performTextClearance()
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+33123456789")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+33 1 23 45 67 89")
    composeTestRule.onNodeWithTag("phoneField").performTextClearance()
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+41791234567")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+41 79 123 45 67")
  }

  @Test
  fun testProfilePictureUpload() {
    // Set the content of the ComposeTestRule
    composeTestRule.setContent {
      SignUpScreen(navigationActions, userViewModel)
    }

    // Wait for the Compose hierarchy to be set up
    composeTestRule.waitForIdle()

    // Create a hardcoded URI that will be returned as the cropping result
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val hardcodedUri = Uri.parse("android.resource://${context.packageName}/raw/profile")

    // Mock the ActivityResultLauncher for uCrop
    val mockLauncher = mock<ActivityResultLauncher<Intent>>()

    // Simulate the uCrop result
    val resultIntent = Intent().apply {
      data = hardcodedUri
    }
    val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)

    // When the launcher is invoked, simulate the result callback with the hardcoded URI
//    doAnswer { invocation ->
//      val callback = invocation.arguments[0] as (Instrumentation.ActivityResult) -> Unit
//      callback(result)  // Pass the hardcoded result
//      null
//    }.`when`(mockLauncher).launch(any())
    doAnswer { invocation ->
      val callback = invocation.getArgument<(Instrumentation.ActivityResult) -> Unit>(0)
      callback(result) // Pass the hardcoded result
      null
    }.whenever(mockLauncher).launch(any())

    // Now proceed with your test and verify behavior after uCrop result
    composeTestRule.onNodeWithTag("profilePictureButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profilePicture").assertExists()
  }

  /*@Test
  fun testProfilePictureUpload() {
    // Ensure that the test rule is correctly initialized
    composeTestRule.setContent {
      SignUpScreen(navigationActions, userViewModel)
    }

    // Mock the cropLauncher and image picking
    val mockLauncher = mock<ActivityResultLauncher<Intent>>()
    val mockCropLauncher = mock<ActivityResultLauncher<Intent>>()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val hardcodedUri = Uri.parse("android.resource://${context.packageName}/raw/profile")

    // Simulate cropping result
    val resultIntent = Intent().apply {
      putExtra(UCrop.EXTRA_OUTPUT_URI, hardcodedUri)
    }
    val cropResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)

    // Set up the mock behavior for cropLauncher
    doAnswer { invocation ->
      val callback = invocation.getArgument<(Instrumentation.ActivityResult) -> Unit>(0)
      callback(cropResult) // Pass the hardcoded result for cropping
      null
    }.whenever(mockCropLauncher).launch(any())

    // Simulate image selection
    val selectedImageUri = Uri.parse("android.resource://${context.packageName}/raw/profile")
    val resultIntentFromPicker = Intent().apply {
      putExtra(Intent.EXTRA_STREAM, selectedImageUri)
    }
    val imageResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntentFromPicker)

    // Mock the launcher behavior to invoke cropping
    doAnswer { invocation ->
      val callback = invocation.getArgument<(Instrumentation.ActivityResult) -> Unit>(0)
      callback(imageResult) // Pass the selected image URI
      null
    }.whenever(mockLauncher).launch(any())

    // Trigger the image picker by clicking the profile picture button
    composeTestRule.onNodeWithTag("profilePictureButton", useUnmergedTree = true).performClick()

    // Optional: Print the current UI hierarchy for debugging
    composeTestRule.onRoot(useUnmergedTree = true).printToLog("Current UI Hierarchy")

    // Check if the profile picture was updated
    composeTestRule.onNodeWithTag("profilePicture", useUnmergedTree = true).assertExists()
  }*/

  /*@Test
  fun testCreateAccountButton_success() { // Not yet implemented as we need to mock the navigation actions
    composeTestRule.setContent {
      SignUpScreen(navigationActions, userViewModel)
    }

    // Fill the form with valid data
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("emailField").performTextInput("john.doe@example.com")
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+123456789")

    // Simulate clicking on the "Create Account" button
    composeTestRule.onNodeWithTag("createAccountButton").performClick()

    // e.g., mockNavigationActions().navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun testCreateAccountButton_failure() { // Need to make sure the create account button fails when the form is incomplete
    composeTestRule.setContent {
      SignUpScreen(navigationActions, userViewModel)
    }

    // Fill the form with some invalid data
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("emailField").performTextInput("")

    // Simulate clicking on the "Create Account" button
    composeTestRule.onNodeWithTag("createAccountButton").performClick()

    // Check for failure Toast
    composeTestRule.onNodeWithText("Account creation failed").assertExists()
  }*/
}
