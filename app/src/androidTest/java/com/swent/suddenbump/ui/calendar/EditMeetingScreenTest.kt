package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting.TestMeetingRepositoryHelper
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
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
class EditMeetingScreenTest {

    private lateinit var navigationActions: NavigationActions
    private lateinit var meetingRepository: MeetingRepository
    private lateinit var meetingViewModel: MeetingViewModel



    @get:Rule val composeTestRule = createComposeRule()

    private val meeting = Meeting(meetingId = "JhXlhoSvTmbtTFSVpNnA", location = "Cafe", date = Timestamp.now(), friendId = "FPHuqGkCBo7Iinbo5OO9", creatorId = "P7vuP4bbEQB03OSR3QwJ")

    @Before
    fun setUp() {
        meetingRepository = mock(MeetingRepository::class.java)
        navigationActions = mock(NavigationActions::class.java)
        meetingViewModel = MeetingViewModel(meetingRepository)

        `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_MEETING)


        composeTestRule.setContent {
            EditMeetingScreen(navigationActions, meetingViewModel)
        }
    }

    @Test
    fun displayAllComponents() {
        // Check top app bar title
        composeTestRule.onNodeWithTag("Edit Meeting").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Edit Meeting").assertTextEquals("Edit Meeting")
        // Check back button
        composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
        // Input location
        composeTestRule.onNodeWithTag("Location").assertIsDisplayed()
        // Input date
        composeTestRule.onNodeWithTag("Date").assertIsDisplayed()
        // Click save button
        composeTestRule.onNodeWithTag("Save Changes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Save Changes").assertTextEquals("Save Changes")
        // Click delete button
        composeTestRule.onNodeWithTag("Delete Meeting").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Delete Meeting").assertTextEquals("Delete Meeting")
    }

    @Test
    fun inputsHaveInitialValue() {
        meetingViewModel.selectMeeting(meeting)
        composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }

        Thread.sleep(10000)

        /*composeTestRule.onNodeWithTag("inputTodoTitle").assertTextContains(todo.name)
        composeTestRule.onNodeWithTag("inputTodoDescription").assertTextContains(todo.description)
        composeTestRule.onNodeWithTag("inputTodoAssignee").assertTextContains(todo.assigneeName)
        composeTestRule.onNodeWithTag("inputTodoDate").assertTextContains("5/9/2024")
        composeTestRule.onNodeWithTag("inputTodoStatus").assertTextContains("Created")*/
    }
}