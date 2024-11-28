package com.swent.suddenbump.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import android.content.Context
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * A utility function to format a date string with slashes.
 *
 * @param input The current text field value.
 * @return The formatted text field value.
 */
fun formatDateString(input: TextFieldValue): TextFieldValue {
  val digitsOnly = input.text.filter { it.isDigit() } // Extract only digits
  val maxLength = 8 // Maximum number of digits (DD MM YYYY)
  val formatted = StringBuilder()
  val cursorIndex = input.selection.start.coerceIn(0, input.text.length)

  var newCursorIndex = cursorIndex

  // Limit digits to 8 characters (DD MM YYYY)
  val trimmedDigits = digitsOnly.take(maxLength)

  // Build the formatted string with slashes
  for (i in trimmedDigits.indices) {
    formatted.append(trimmedDigits[i])

    // Insert slashes at positions 2 and 5
    if ((i == 1 || i == 3) && i < trimmedDigits.lastIndex) {
      formatted.append('/')
      if (i < cursorIndex) {
        newCursorIndex++
      }
    }
  }

  // Ensure cursor is within bounds
  newCursorIndex = newCursorIndex.coerceIn(0, formatted.length)

  return TextFieldValue(text = formatted.toString(), selection = TextRange(newCursorIndex))
}

/**
 * Opens a DatePickerDialog and updates the provided date state.
 *
 * @param context The context used to show the DatePickerDialog.
 * @param initialDate The initial date to show in the picker (default is today).
 * @param onDateSelected Callback to update the selected date.
 */
fun showDatePickerDialog(
    context: Context,
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (TextFieldValue) -> Unit
) {
    val year = initialDate.get(Calendar.YEAR)
    val month = initialDate.get(Calendar.MONTH)
    val day = initialDate.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
            }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            onDateSelected(TextFieldValue(dateFormat.format(selectedCalendar.time)))
        },
        year,
        month,
        day
    )

    datePickerDialog.show()
}

