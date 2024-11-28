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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.formatDateString
import com.swent.suddenbump.ui.utils.showDatePickerDialog
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
  var date by remember { mutableStateOf(TextFieldValue("")) }
  var showDatePicker by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val friendId = userViewModel.user?.uid ?: ""

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
              // Location field
              OutlinedTextField(
                  value = location,
                  onValueChange = { location = it },
                  label = { Text("Location") },
                  textStyle = LocalTextStyle.current.copy(color = Color.White),
                  modifier = Modifier.fillMaxWidth().testTag("Location"))

              // Date Field (Non-clickable)
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
                              location = location,
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
                  colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Purple40),
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
