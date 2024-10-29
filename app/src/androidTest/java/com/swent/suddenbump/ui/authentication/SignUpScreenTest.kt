package com.swent.suddenbump.ui.authentication

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SignUpScreenTest {

  private lateinit var userRepository: UserRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseAuthMock: MockedStatic<FirebaseAuth>
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  //  @Mock
  //  lateinit var mockLauncher: ActivityResultLauncher<Intent> // not used yet - needed to mock
  // uCrop

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)
    /*firebaseAuth = mock(FirebaseAuth::class.java)

    // Mock the static method getInstance()
    firebaseAuthMock = mockStatic(FirebaseAuth::class.java)
    firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuth)

    doNothing().`when`(firebaseAuth).signOut()
    `when`(firebaseAuth.currentUser?.email).thenReturn("john.doe@example.com")*/
  }

  /*@After
  fun tearDown() {
    // Check if firebaseAuthMock is initialized before closing it
    if (::firebaseAuthMock.isInitialized) {
      firebaseAuthMock.close()
    }
  }*/

  @Test
  fun testSignUpScreen_initialState() {
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }

    // Check if fields are displayed
    composeTestRule.onNodeWithTag("firstNameField").assertExists()
    composeTestRule.onNodeWithTag("lastNameField").assertExists()
    composeTestRule.onNodeWithTag("emailField").assertExists()
    composeTestRule.onNodeWithTag("emailField").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("phoneField").assertExists()
    composeTestRule.onNodeWithTag("createAccountButton").assertExists()

    // Check that the profile picture button is displayed
    composeTestRule.onNodeWithTag("noProfilePic", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testInputFields_interaction() {
    // Set the initial content for testing
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }

    `when`(userViewModel.getNewUid()).thenReturn("UnIntGenre5")

    // Test user input in the text fields
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("firstNameField").assertTextContains("John")

    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("lastNameField").assertTextContains("Doe")

    composeTestRule.onNodeWithTag("emailField").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("phoneField").performTextInput("+1234567890")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+1 234-567-890")

    composeTestRule.onNodeWithTag("createAccountButton").performClick()
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

    // Perform button click
    composeTestRule.onNodeWithTag("profilePictureButton").assertHasClickAction()
    //        composeTestRule.onNodeWithTag("noProfilePic").assertIsDisplayed()

    // Perform create account button click
    //    composeTestRule.onNodeWithTag("createAccountButton").performClick()
    //    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }

  /*@Test
  fun testProfilePictureUpload() { // Having difficulty setting up this test and mocking uCrop
    // Set up the content in the test
    composeTestRule.setContent {
      SignUpScreen(navigationActions, userViewModel)
    }

    // Prepare the Uri for the cropped image
    val croppedImageUri = Uri.parse("content://path/to/cropped/image.jpg")
    val resultIntent = Intent().apply {
      putExtra(UCrop.EXTRA_OUTPUT_URI, croppedImageUri)
    }

    // Mock the result from UCrop
    val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)

    // Mock the behavior of the UCrop launcher
    `when`(mockLauncher.launch(any())).then {
      mockLauncher.launch(resultIntent)  // This is just an illustrative example
    }


    // Trigger the profile picture upload by clicking the button
    composeTestRule.onNodeWithTag("profilePictureButton").performClick()

    // Here you would simulate the result of the UCrop in the launcher
    // This assumes your UI reacts to the result being set
    mockLauncher.launch(resultIntent)

    // Verify that the profile picture was uploaded correctly
    composeTestRule.onNodeWithTag("profilePicture").assertExists()
  }

  @Test
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
