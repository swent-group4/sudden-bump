package com.swent.suddenbump.ui.calendar

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.Timestamp
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

/**
 * A composable function that displays a button for adding or editing a meeting.
 *
 * @param addOrEdit A boolean indicating whether the button is for adding (true) or editing (false)
 *   a meeting.
 * @param date The date input as a [TextFieldValue].
 * @param context The context in which the button is used.
 * @param navigationActions An instance of [NavigationActions] to handle navigation events.
 * @param buttonHelper A callback function to handle the meeting date as a [Timestamp].
 */
@Composable
fun AddAndEditMeetingButton(
    addOrEdit: Boolean,
    date: TextFieldValue,
    context: Context,
    navigationActions: NavigationActions,
    buttonHelper: (Timestamp) -> Unit
) {
  Button(
      onClick = {
        try {
          // Log the input for debugging
          if (addOrEdit) {
            Log.d("AddMeetingScreen", "Raw date input: ${date.text}")
          } else {
            Log.d("EditMeetingScreen", "Raw date input: ${date.text}")
          }

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
              dateFormat.parse(date.text) ?: throw IllegalArgumentException("Failed to parse date")

          // Log parsed date for debugging
          if (addOrEdit) {
            Log.d("AddMeetingScreen", "Parsed date: $parsedDate")
          } else {
            Log.d("EditMeetingScreen", "Parsed date: $parsedDate")
          }

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

          buttonHelper(meetingDate)

          navigationActions.goBack()
        } catch (e: IllegalArgumentException) {
          if (addOrEdit) {
            Log.e("AddMeetingScreen", "Invalid date input: ${date.text}", e)
          } else {
            Log.e("EditMeetingScreen", "Invalid date input: ${date.text}", e)
          }
          Toast.makeText(context, "Invalid date: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
          if (addOrEdit) {
            Log.e("AddMeetingScreen", "Error parsing date: ${date.text}", e)
          } else {
            Log.e("EditMeetingScreen", "Error parsing date: ${date.text}", e)
          }
          Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
        }
      },
      colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Pinkish),
      modifier =
          Modifier.fillMaxWidth().testTag(if (addOrEdit) "Save Meeting" else "Save Changes")) {
        Text(if (addOrEdit) "Ask to Meet" else "Save Changes")
      }
}
