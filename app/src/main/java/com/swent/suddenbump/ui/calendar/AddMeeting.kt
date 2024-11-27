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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText


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
                    onValueChange = { newValue ->
                        date = formatDateString(newValue)
                    },
                    label = { Text("Date (dd/MM/yyyy)") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("Date"),
                    visualTransformation = DateVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.testTag("DateIconButton")
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
                            dateFormat.isLenient = false
                            val parsedDate = dateFormat.parse(date.text)
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
                                creatorId = userViewModel.getCurrentUser().value?.uid ?: "",
                                accepted = false
                            )
                            meetingViewModel.addMeeting(newMeeting)
                            Toast.makeText(context, "Meeting request sent", Toast.LENGTH_SHORT).show()
                            navigationActions.goBack()
                        } catch (e: Exception) {
                            Log.e("AddMeetingScreen", "Error parsing date", e)
                            Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("Save Meeting")
                ) {
                    Text("Ask to Meet")
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
                            date = TextFieldValue(dateFormat.format(selectedCalendar.time))
                            showDatePicker = false
                        },
                        year,
                        month,
                        day
                    )

                    datePickerDialog.show()
                }
            }
        }
    )
}

fun formatDateString(input: TextFieldValue): TextFieldValue {
    val digitsOnly = input.text.filter { it.isDigit() } // Extract only digits
    val formatted = StringBuilder()

    // Add slashes at correct positions: after day (2nd digit) and month (4th digit)
    for (i in digitsOnly.indices) {
        formatted.append(digitsOnly[i])
        if ((i == 1 || i == 3) && i < digitsOnly.length - 1) {
            formatted.append('/')
        }
    }

    // Adjust the cursor position for the added slashes
    val originalCursorPosition = input.selection.start
    val slashCountBeforeCursor = input.text.take(originalCursorPosition).count { it == '/' }
    val expectedSlashCount = formatted.count { it == '/' }

    // Update cursor position based on added/removed slashes
    var newCursorPosition = originalCursorPosition + (expectedSlashCount - slashCountBeforeCursor)
    newCursorPosition = newCursorPosition.coerceIn(0, formatted.length)

    return TextFieldValue(formatted.toString(), TextRange(newCursorPosition))
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.filter { it.isDigit() } // Keep only digits
        val formatted = StringBuilder()

        // Build formatted text with slashes
        for (i in trimmed.indices) {
            formatted.append(trimmed[i])
            if ((i == 1 || i == 3) && i < trimmed.length - 1) {
                formatted.append('/')
            }
        }

        // Map cursor positions
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    offset <= 6 -> offset + 2
                    else -> offset + 2
                }.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 8 -> offset - 2
                    else -> offset - 2
                }.coerceAtMost(trimmed.length)
            }
        }

        return TransformedText(AnnotatedString(formatted.toString()), offsetMapping)
    }
}
