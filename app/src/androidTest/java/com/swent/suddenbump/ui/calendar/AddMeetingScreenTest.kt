package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting.TestMeetingRepositoryHelper
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class AddMeetingScreenTest {

    private lateinit var navigationActions: NavigationActions

    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var meetingRepository: MeetingRepository

    private lateinit var userFriendsFlow: StateFlow<List<User>>

    @Mock private lateinit var userViewModel: UserViewModel
    @Mock private lateinit var chatRepository: ChatRepository

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        navigationActions = mock(NavigationActions::class.java)
        userRepository = mock(UserRepository::class.java)
        chatRepository = mock(ChatRepository::class.java)
        userViewModel = spy(UserViewModel(userRepository, chatRepository))
        `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

        meetingRepository = TestMeetingRepositoryHelper()
        meetingViewModel = MeetingViewModel(meetingRepository)

        composeTestRule.setContent {
            AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
        }
    }

    @Test
    fun testAddMeetingScreen() {
        // Check top app bar title
        composeTestRule.onNodeWithTag("Add New Meeting").assertExists()
        // Check back button
        composeTestRule.onNodeWithTag("Back").assertExists().performClick()
        // Input location
        composeTestRule.onNodeWithTag("Location").performTextInput("Central Park")
        // Input date
        composeTestRule.onNodeWithTag("Date").performTextInput("25/12/2024")
        // Click save button
        composeTestRule.onNodeWithTag("Save Meeting").performClick()
    }
}

