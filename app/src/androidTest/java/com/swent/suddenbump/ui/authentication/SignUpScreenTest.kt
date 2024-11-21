package com.swent.suddenbump.ui.authentication

import android.util.Log
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
import com.google.firebase.auth.FirebaseUser
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SignUpScreenTest {

  private lateinit var userRepository: UserRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(userRepository, chatRepository)

    // Mock FirebaseAuth and FirebaseUser
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn("test@example.com")
  }

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
    composeTestRule.onNodeWithTag("createAccountButton").assertIsNotEnabled()

    // Check that the profile picture button is displayed
    composeTestRule.onNodeWithTag("noProfilePic", useUnmergedTree = true).assertExists()

    // Check that the profile picture button is clickable
    composeTestRule.onNodeWithTag("profilePictureButton").assertHasClickAction()

    composeTestRule.onNodeWithTag("sendCodeButton").assertExists()
    composeTestRule.onNodeWithTag("sendCodeButton").assertIsNotEnabled()
  }

  @Test
  fun testInputFields_interaction() {
    Log.d("SignUpDebug", "user last name : ${userViewModel.user?.lastName}")
    // Set the initial content for testing
    composeTestRule.setContent { SignUpScreen(navigationActions, userViewModel) }

    // Test user input in the text fields
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("firstNameField").assertTextContains("John")

    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("lastNameField").assertTextContains("Doe")

    composeTestRule.onNodeWithTag("emailField").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("sendCodeButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("createAccountButton").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("phoneField").performTextInput("+33613507628")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+33 6 13 50 76 28")

    composeTestRule.onNodeWithTag("sendCodeButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("sendCodeButton").performClick()

    composeTestRule.onNodeWithTag("codeField").assertExists()
    composeTestRule.onNodeWithTag("verifyCodeButton").assertExists()
    composeTestRule.onNodeWithTag("verifyCodeButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("codeField").performTextInput("123456")
    composeTestRule.onNodeWithTag("verifyCodeButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("verifyCodeButton").performClick()

    composeTestRule.onNodeWithTag("createAccountButton").assertHasClickAction()
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
  }
}
