package com.swent.suddenbump.ui.calendar

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
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
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val friendId = userViewModel.user?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Meeting with ${userViewModel.user?.firstName ?: ""}",
                        color = Color.White,
                        modifier = Modifier.testTag("Add New Meeting")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("Back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("Location")
                )

                // Date Field (Non-clickable)
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date (dd/MM/yyyy)") },
                    textStyle = TextStyle(color = Color.White),
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Pick a date")
                        }
                    }
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

                            val newMeeting = Meeting(
                                meetingId = meetingViewModel.getNewMeetingid(),
                                friendId = friendId,
                                location = location,
                                date = meetingDate,
                                creatorId = userViewModel.getCurrentUser().value?.uid ?: ""
                            )
                            meetingViewModel.addMeeting(newMeeting)
                            Toast.makeText(context, "Meeting created successfully", Toast.LENGTH_SHORT).show()
                            navigationActions.goBack()
                        } catch (e: Exception) {
                            Log.e("AddMeetingScreen", "Error parsing date", e)
                            Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("Save Meeting")
                ) {
                    Text("Save Meeting")
                }

                // Show Date Picker Dialog if needed
                if (showDatePicker) {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = android.app.DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, y)
                                set(Calendar.MONTH, m)
                                set(Calendar.DAY_OF_MONTH, d)
                            }
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            date = dateFormat.format(selectedCalendar.time)
                            showDatePicker = false
                        }, year, month, day
                    )
                    datePickerDialog.show()
                }
            }
        }
    )
}









