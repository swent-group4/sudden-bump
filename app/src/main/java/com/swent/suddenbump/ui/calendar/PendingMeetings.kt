package com.swent.suddenbump.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions

/**
 * Composable function to display the Pending Meetings screen.
 *
 * @param navigationActions Actions for navigation.
 * @param meetingViewModel ViewModel for managing meetings.
 * @param userViewModel ViewModel for managing user data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingMeetingsScreen(
    navigationActions: NavigationActions,
    meetingViewModel: MeetingViewModel,
    userViewModel: UserViewModel
) {
  LaunchedEffect(Unit) {
      meetingViewModel.getMeetings()
      meetingViewModel.deleteExpiredMeetings()
  }
  val meetings by meetingViewModel.meetings.collectAsState()
  val userFriends by userViewModel.getUserFriends().collectAsState(initial = emptyList())
  val currentUserId = userViewModel.getCurrentUser().value?.uid ?: ""

  Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Text(
                  "Pending Meetings",
                  color = Color.White,
                  modifier = Modifier.testTag("Pending Meetings"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("Back")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
        )
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .background(Color.Black)
                    .fillMaxSize()
                    .testTag("pendingMeetingsScreen")) {
              ScrollablePendingMeetings(
                  meetings = meetings.filter { !it.accepted && it.friendId == currentUserId },
                  userFriends = userFriends,
                  meetingViewModel = meetingViewModel)
            }
      })
}

/**
 * Composable function to display a scrollable list of pending meetings.
 *
 * @param meetings List of meetings to display.
 * @param userFriends List of user friends.
 * @param meetingViewModel ViewModel for managing meetings.
 */
@Composable
fun ScrollablePendingMeetings(
    meetings: List<Meeting>,
    userFriends: List<User>,
    meetingViewModel: MeetingViewModel,
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(meetings) { meeting ->
          PendingMeetingRow(
              meeting = meeting, userFriends = userFriends, meetingViewModel = meetingViewModel)
        }
      }
}

/**
 * Composable function to display a single row for a pending meeting.
 *
 * @param meeting The meeting data.
 * @param userFriends List of user friends.
 * @param meetingViewModel ViewModel for managing meetings.
 */
@Composable
fun PendingMeetingRow(
    meeting: Meeting,
    userFriends: List<User>,
    meetingViewModel: MeetingViewModel
) {
  val friend = userFriends.find { it.uid == meeting.friendId || it.uid == meeting.creatorId }
  val friendName = friend?.let { "${it.firstName} ${it.lastName.first()}." } ?: "Unknown Friend"
  val formattedDate = formatDate(meeting.date.toDate())

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color.White)
              .padding(16.dp)
              .testTag("pendingMeetingRow")) {
        Row(
            modifier =
                Modifier.fillMaxWidth().clickable {
                  // Handle row click if needed
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = "https://avatar.iran.liara.run/public/42",
                        contentDescription = null,
                        modifier =
                            Modifier.width(50.dp)
                                .height(50.dp)
                                .padding(8.dp)
                                .testTag("profileImage"))
                    Column {
                      Text(
                          text = friendName,
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.Bold),
                          modifier = Modifier.testTag("userName"))
                      Text(
                          text = "Meet at ${meeting.location} on $formattedDate",
                          style = MaterialTheme.typography.bodyMedium,
                          color = Color.Gray,
                          modifier = Modifier.testTag("meetingDetails"))
                    }
                  }
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                      val updatedMeeting = meeting.copy(accepted = true)
                      meetingViewModel.updateMeeting(updatedMeeting)
                    },
                    modifier = Modifier.testTag("acceptButton")) {
                      Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Accept",
                          tint = Color.Green)
                    }
                IconButton(
                    onClick = { meetingViewModel.deleteMeeting(meeting.meetingId) },
                    modifier = Modifier.testTag("denyButton")) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Decline",
                          tint = Color.Red)
                    }
              }
            }
        HorizontalDivider(
            color = Color.Gray,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 4.dp).testTag("divider"))
      }
}
