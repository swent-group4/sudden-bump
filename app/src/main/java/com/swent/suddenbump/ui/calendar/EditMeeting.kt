package com.swent.suddenbump.ui.calendar

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting_location.Location
import com.swent.suddenbump.model.meeting_location.LocationViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.LocationField
import com.swent.suddenbump.ui.utils.formatDateString
import com.swent.suddenbump.ui.utils.showDatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingScreen(
    navigationActions: NavigationActions,
    meetingViewModel: MeetingViewModel,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  val meeting = meetingViewModel.selectedMeeting.collectAsState().value ?: return

  var date by remember {
    mutableStateOf(
        meeting.date.toDate().let {
          TextFieldValue(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it))
        })
  }
  var showDatePicker by remember { mutableStateOf(false) }
  var selectedLocation by remember { mutableStateOf(meeting.location) }
  var locationQuery by remember { mutableStateOf(meeting.location?.name) }

  // State for dropdown visibility
  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text("Edit Meeting", color = Color.White, modifier = Modifier.testTag("Edit Meeting"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("Back")) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
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
              LocationField(
                  locationQuery = locationQuery,
                  onLocationQueryChange = {
                    locationQuery = it
                    locationViewModel.setQuery(it)
                  },
                  locationSuggestions = locationSuggestions,
                  onLocationSelected = { selectedLocation = it },
                  showDropdown = showDropdown,
                  onDropdownChange = { showDropdown = it })

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

              val editButtonHelper: (Timestamp) -> Unit = { meetingDate ->
                // Check if selectedLocation is a valid Location
                if (selectedLocation == null || locationQuery != selectedLocation?.name) {
                  throw IllegalArgumentException("Location must be selected")
                }
                // Save the updated meeting
                val updatedMeeting = meeting.copy(location = selectedLocation, date = meetingDate)
                meetingViewModel.updateMeeting(updatedMeeting)

                Toast.makeText(context, "Meeting updated successfully", Toast.LENGTH_SHORT).show()
              }

              AddAndEditMeetingButton(false, date, context, navigationActions, editButtonHelper)

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                  onClick = {
                    meeting.let {
                      meetingViewModel.deleteMeeting(it.meetingId)
                      Toast.makeText(context, "Meeting deleted successfully", Toast.LENGTH_SHORT)
                          .show()
                      navigationActions.goBack()
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag("Delete Meeting"),
                  colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Gray),
              ) {
                Text("Delete Meeting")
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
