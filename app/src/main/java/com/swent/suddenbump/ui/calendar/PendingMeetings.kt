package com.swent.suddenbump.ui.calendar

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Gray
import com.swent.suddenbump.ui.theme.Pinkish
import com.swent.suddenbump.ui.utils.UserProfileImage
import java.text.SimpleDateFormat
import java.util.Locale

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
  LaunchedEffect(Unit) { meetingViewModel.getMeetings() }
  val meetings by meetingViewModel.meetings.collectAsState()
  val userFriends by userViewModel.getUserFriends().collectAsState(initial = emptyList())
  val currentUserId = userViewModel.getCurrentUser().value.uid ?: ""

  Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Text(
                  "Meeting Requests",
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
  val dayFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
  val friend = userFriends.find { it.uid == meeting.friendId || it.uid == meeting.creatorId }
  val friendName = friend?.let { "${it.firstName} ${it.lastName.first()}." } ?: "Unknown Friend"

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Gray, MaterialTheme.shapes.medium)
              .padding(16.dp)
              .testTag("pendingMeetingRow")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              // Date Text
              Text(
                  text = dayFormat.format(meeting.date.toDate()),
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                  modifier = Modifier.padding(end = 8.dp).testTag("meetingDate"))
              // Buttons Row
              Row(
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()) {
                    // Accept Button
                    IconButton(
                        onClick = {
                          val updatedMeeting = meeting.copy(accepted = true)
                          meetingViewModel.updateMeeting(updatedMeeting)
                        },
                        modifier = Modifier.testTag("acceptButton").size(36.dp)) {
                          Icon(
                              imageVector = Icons.Default.Check,
                              contentDescription = "Accept",
                              tint = Color.White,
                              modifier = Modifier.size(24.dp))
                        }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Decline Button
                    IconButton(
                        onClick = { meetingViewModel.deleteMeeting(meeting.meetingId) },
                        modifier = Modifier.testTag("denyButton").size(36.dp)) {
                          Icon(
                              imageVector = Icons.Default.Close,
                              contentDescription = "Decline",
                              tint = Color.White,
                              modifier = Modifier.size(24.dp))
                        }
                  }
            }

        Spacer(modifier = Modifier.height(8.dp))

        // Invitation Details Row
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
              Card(
                  modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("meetingCard"),
                  colors = CardDefaults.cardColors(containerColor = Pinkish)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                          // Profile Image
                          if (friend != null) {
                            UserProfileImage(friend, 40)
                          }

                          // Invitation Text
                          Text(
                              text = "$friendName invites you to meet at ${meeting.location?.name}",
                              style = MaterialTheme.typography.bodyMedium,
                              color = Color.White,
                              modifier = Modifier.testTag("meetingDetails"))
                        }
                  }
            }
      }
}
