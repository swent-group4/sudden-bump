package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(UserViewModel::class)
@RunWith(AndroidJUnit4::class)
class EndToEndTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject
  lateinit var userViewModel: UserViewModel

  private lateinit var mockFirestore: UserRepositoryFirestore
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockChatRepository: ChatRepositoryFirestore
  private lateinit var mockMessagesCollection: CollectionReference
  private lateinit var mockMessageDocument: DocumentReference
  private lateinit var mockQuery: Query
  private lateinit var mockUserViewModel: UserViewModel

  private val location =
      Location("mockProvider").apply {
        latitude = 37.7749
        longitude = -122.4194
      }

  @Before
  fun setUp() {
    // Mock Firestore, FirebaseAuth, and other dependencies
    mockFirestore = mockk(relaxed = true)
    mockAuth = mockk(relaxed = true)
    mockChatRepository = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)

    userViewModel = UserViewModel(mockFirestore, mockChatRepository)

    val currentUserId = "user2"
    val friendId = "1"
    val chatId = "chat789"
    val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot1 = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot2 = mockk<DocumentSnapshot>(relaxed = true)




    hiltRule.inject()
  }



  @Test
  fun fullAppNavigationTest() {
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2: Navigate to Friends List
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    // Step 3: Wait for and navigate to Contact screen
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag("userList").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag("userList").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("contactScreen").assertExists()

    // Step 4: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 5: Navigate to Settings screen
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    // Step 6: Scroll and navigate to Storage and Data screen
    composeTestRule
        .onNodeWithTag("settingsLazyColumn")
        .performScrollToNode(hasTestTag("StorageAndDataOption")) // Scroll to make option visible
    composeTestRule.onNodeWithTag("StorageAndDataOption").performClick()
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag("storageAndDataScreen").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag("storageAndDataScreen").assertExists()

    // Step 7: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
  }


  @Test
  fun testSendMessageEoE() {

    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // part to debug, instead of clicking on a user's profile on the overview, go through
    // conversation scree/message screen
      // Step 2: Navigate to user row and send message

    composeTestRule.onNodeWithTag("1").assertExists().performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Hello, how are you?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag("ChatInputTextBox")
      .performTextInput("Do you want to meet at Rolex today at 10?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()
  }
}
