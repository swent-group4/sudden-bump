package com.swent.suddenbump.model.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toCustomFormat(): String {
    val format = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
    return format.format(this)
}