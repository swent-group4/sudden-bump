package com.swent.suddenbump.ui.calendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting_location.Location
import com.swent.suddenbump.model.meeting_location.LocationViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.utils.formatDateString
import com.swent.suddenbump.ui.utils.showDatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {

  var date by remember { mutableStateOf(TextFieldValue("")) }
  var showDatePicker by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val friendId = userViewModel.user?.uid ?: ""
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    val locationQuery by locationViewModel.query.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }
    val locationSuggestions by
    locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Ask ${userViewModel.user?.firstName ?: ""} to Meet",
                  color = Color.White,
                  modifier = Modifier.testTag("Add New Meeting"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("Back")) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.padding(padding).fillMaxSize().background(Color.Black).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Location field
            ExposedDropdownMenuBox(
                expanded = showDropdown && locationSuggestions.isNotEmpty(),
                onExpandedChange = { showDropdown = it } // Toggle dropdown visibility
            ) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = {
                        locationViewModel.setQuery(it)
                        showDropdown = true // Show dropdown when user starts typing
                    },
                    label = { Text("Location") },
                    textStyle = TextStyle(color = Color.White),
                    placeholder = { Text("Enter an Address or Location") },
                    modifier =
                    Modifier.menuAnchor() // Anchor the dropdown to this text field
                        .fillMaxWidth()
                        .testTag("inputTodoLocation"),
                    singleLine = true)

                // Dropdown menu for location suggestions
                ExposedDropdownMenu(
                    expanded = showDropdown && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.background(Color.Black) ) {
                    locationSuggestions.filterNotNull().take(3).forEach { location ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                    location.name.take(30) +
                                            if (location.name.length > 30) "..."
                                            else "", // Limit name length
                                    color = Purple40,
                                    maxLines = 1 // Ensure name doesn't overflow
                                )
                            },
                            onClick = {
                                // Extract substring up to the first comma
                                val trimmedLocation = location.name.substringBefore(",")
                                locationViewModel.setQuery(trimmedLocation) // Update the query with trimmed location
                                selectedLocation = location.copy(name = trimmedLocation) // Save the trimmed name
                                showDropdown = false // Close dropdown on selection
                            },
                            modifier = Modifier.padding(8.dp).background(Color.Black))
                    }
                }
            }

              // Date Field
              OutlinedTextField(
                  value = date,
                  onValueChange = { newValue ->
                    date = formatDateString(newValue) // Ensure raw input is properly formatted
                  },
                  label = { Text("Date (dd/MM/yyyy)") },
                  textStyle = TextStyle(color = Color.White),
                  modifier = Modifier.fillMaxWidth().testTag("Date"),
                  trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.testTag("DateIconButton")) {
                          Icon(Icons.Filled.Edit, contentDescription = "Pick a date")
                        }
                  })

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                  onClick = {
                    try {
                      // Log the input for debugging
                      Log.d("AddMeetingScreen", "Raw date input: ${date.text}")

                      // Validate input length (must be exactly "DD/MM/YYYY")
                      if (date.text.length != 10) {
                        throw IllegalArgumentException("Date must be in the format DD/MM/YYYY")
                      }

                      // Parse the date
                      val dateFormat =
                          SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            isLenient = false // Strict parsing
                          }

                      val parsedDate =
                          dateFormat.parse(date.text)
                              ?: throw IllegalArgumentException("Failed to parse date")

                      // Log parsed date for debugging
                      Log.d("AddMeetingScreen", "Parsed date: $parsedDate")

                      // Check if the date is in the past
                      val today =
                          GregorianCalendar().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                          }
                      val inputCalendar =
                          GregorianCalendar().apply {
                            time = parsedDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                          }

                      if (inputCalendar.before(today)) {
                        throw IllegalArgumentException("Date is in the past")
                      }

                      val meetingDate = Timestamp(inputCalendar.time)

                      // Create and save the meeting
                      val newMeeting =
                          Meeting(
                              meetingId = meetingViewModel.getNewMeetingid(),
                              friendId = friendId,
                              location = selectedLocation,
                              date = meetingDate,
                              creatorId = userViewModel.getCurrentUser().value.uid,
                              accepted = false)
                      meetingViewModel.addMeeting(newMeeting)
                      Toast.makeText(context, "Meeting request sent", Toast.LENGTH_SHORT).show()
                      navigationActions.goBack()
                    } catch (e: IllegalArgumentException) {
                      Log.e("AddMeetingScreen", "Invalid date input: ${date.text}", e)
                      Toast.makeText(context, "Invalid date: ${e.message}", Toast.LENGTH_SHORT)
                          .show()
                    } catch (e: Exception) {
                      Log.e("AddMeetingScreen", "Error parsing date: ${date.text}", e)
                      Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                    }
                  },
                  colors = ButtonDefaults.buttonColors(Purple40),
                  modifier = Modifier.fillMaxWidth().testTag("Save Meeting")) {
                    Text("Ask to Meet")
                  }

              // Show Date Picker Dialog if needed
              if (showDatePicker) {
                showDatePickerDialog(
                    context = context,
                    onDateSelected = { selectedDate ->
                      date = selectedDate
                      showDatePicker = false
                    })
              }
            }
      })
}

fun showMeetingScheduledNotification(context: Context, meeting: Meeting) {
  val channelId = "meeting_scheduled_channel"
  val channelName = "Meeting Scheduled Notifications"
  val notificationId = 2

  val notificationChannel =
      NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
  val notificationManager = context.getSystemService(NotificationManager::class.java)
  notificationManager?.createNotificationChannel(notificationChannel)

  // Intent to navigate to the meeting details screen
  val intent =
      Intent(context, MainActivity::class.java).apply {
        putExtra("destination", "Screen.PENDING_MEETINGS")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }

  val pendingIntent: PendingIntent =
      PendingIntent.getActivity(
          context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

  val notificationBuilder =
      NotificationCompat.Builder(context, channelId)
          .setSmallIcon(android.R.drawable.ic_dialog_info)
          .setContentTitle("New Meeting Request")
          .setContentText(
              "You have a new meeting request at ${meeting.location?.name} on ${
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(meeting.date.toDate())
                }.")
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)

  try {
    with(NotificationManagerCompat.from(context)) {
      notify(notificationId, notificationBuilder.build())
    }
  } catch (e: SecurityException) {
    Log.e("NotificationError", "Notification permission not granted", e)
  }
}
