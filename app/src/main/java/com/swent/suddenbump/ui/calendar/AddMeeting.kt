package com.swent.suddenbump.ui.calendar

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel
) {
  var location by remember { mutableStateOf("") }
  var date by remember { mutableStateOf("") }
  val friendId = userViewModel.user?.uid ?: ""
  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Schedule New Meeting with ${userViewModel.user?.firstName?: ""} ${userViewModel.user?.lastName?: ""}",
                  color = Color.White,
                  modifier = Modifier.testTag("Add New Meeting"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("Back")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.padding(padding).fillMaxSize().background(Color.Black).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                  value = location,
                  onValueChange = { location = it },
                  label = { Text("Location") },
                  textStyle = LocalTextStyle.current.copy(color = Color.White),
                  modifier = Modifier.fillMaxWidth().testTag("Location"))
              OutlinedTextField(
                  value = date,
                  onValueChange = { date = it },
                  label = { Text("Date (dd/MM/yyyy)") },
                  textStyle = LocalTextStyle.current.copy(color = Color.White),
                  modifier = Modifier.fillMaxWidth().testTag("Date"))
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                  onClick = {
                    try {
                      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      val parsedDate = dateFormat.parse(date)
                      val calendar =
                          GregorianCalendar().apply {
                            time = parsedDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                          }
                      val meetingDate = Timestamp(calendar.time)

                      val newMeeting =
                          Meeting(
                              meetingId = meetingViewModel.getNewMeetingid(),
                              friendId = friendId,
                              location = location,
                              date = meetingDate,
                              creatorId = userViewModel.getCurrentUser().value?.uid ?: "")
                      meetingViewModel.addMeeting(newMeeting)
                      Toast.makeText(context, "Meeting created successfully", Toast.LENGTH_SHORT)
                          .show()
                      navigationActions.goBack()
                    } catch (e: Exception) {
                      Log.e("AddMeetingScreen", "Error parsing date", e)
                      Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag("Save Meeting")) {
                    Text("Save Meeting")
                  }
            }
      })
}
