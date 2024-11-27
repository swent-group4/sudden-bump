package com.swent.suddenbump.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


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