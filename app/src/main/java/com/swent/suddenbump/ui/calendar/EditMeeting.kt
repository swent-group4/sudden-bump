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
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Pink40
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingScreen(
    navigationActions: NavigationActions,
    meetingViewModel: MeetingViewModel
) {
    val meeting = meetingViewModel.selectedMeeting.collectAsState().value ?: return

    var location by remember { mutableStateOf(meeting.location) }
    var date by remember { mutableStateOf(meeting.date.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Meeting", color = Color.White, modifier = Modifier.testTag("Edit Meeting")) },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.testTag("Back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("Location")
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (dd/MM/yyyy)") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("Date")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        try {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val parsedDate = dateFormat.parse(date)
                            val calendar = GregorianCalendar().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val meetingDate = Timestamp(calendar.time)

                            val updatedMeeting = meeting?.copy(
                                location = location,
                                date = meetingDate
                            )
                            if (updatedMeeting != null) {
                                meetingViewModel.updateMeeting(updatedMeeting)
                                Toast.makeText(context, "Meeting updated successfully", Toast.LENGTH_SHORT).show()
                                navigationActions.goBack()
                            }
                        } catch (e: Exception) {
                            Log.e("EditMeetingScreen", "Error parsing date", e)
                            Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("Save Changes")
                ) {
                    Text("Save Changes")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        meeting?.let {
                            meetingViewModel.deleteMeeting(it.meetingId)
                            Toast.makeText(context, "Meeting deleted successfully", Toast.LENGTH_SHORT).show()
                            navigationActions.goBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("Delete Meeting"),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink40)
                ) {
                    Text("Delete Meeting")
                }
            }
        }
    )
}

