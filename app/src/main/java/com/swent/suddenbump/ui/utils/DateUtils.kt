package com.swent.suddenbump.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A utility function to format a date string with slashes.
 *
 * @param input The current text field value.
 * @return The formatted text field value.
 */
fun formatDateString(input: TextFieldValue): TextFieldValue {
    val digitsOnly = input.text.filter { it.isDigit() } // Extract only digits
    val originalCursorIndex = input.selection.start.coerceIn(0, input.text.length)

    val formatted = StringBuilder()
    val slashPositions = listOf(2, 5) // Positions where slashes should be added
    var digitCount = 0
    var newCursorIndex = 0

    // Build the formatted string and adjust the cursor
    for (i in digitsOnly.indices) {
        // Add the current digit
        formatted.append(digitsOnly[i])
        digitCount++

        // Add slashes at the appropriate positions
        if (digitCount in slashPositions && digitCount < digitsOnly.length) {
            formatted.append('/')
        }

        // Adjust the cursor position
        if (digitCount <= originalCursorIndex) {
            newCursorIndex = formatted.length
        }
    }

    // Ensure the cursor skips slashes during deletion
    if (newCursorIndex > 0 && formatted.getOrNull(newCursorIndex - 1) == '/') {
        newCursorIndex--
    }

    return TextFieldValue(
        text = formatted.toString().take(10), // Limit to "DD/MM/YYYY"
        selection = TextRange(newCursorIndex)
    )
}


/**
 * A visual transformation that formats a date string with slashes.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }
        val formatted = StringBuilder()

        // Build the formatted string (DD/MM/YYYY)
        for (i in digitsOnly.indices) {
            formatted.append(digitsOnly[i])
            if (i == 1 || i == 3) {
                formatted.append('/')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                if (offset > 1) transformedOffset++
                if (offset > 3) transformedOffset++
                return transformedOffset.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = offset
                if (offset > 2) originalOffset--
                if (offset > 5) originalOffset--
                return originalOffset.coerceAtMost(digitsOnly.length)
            }
        }

        return TransformedText(
            text = AnnotatedString(formatted.toString().take(10)), // Ensure max length is 10
            offsetMapping = offsetMapping
        )
    }
}

