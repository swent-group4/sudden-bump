package com.swent.suddenbump.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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
