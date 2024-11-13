package com.swent.suddenbump.model.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.toCustomFormat(): String {
  val today = Calendar.getInstance()
  val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

  val thisCalendar = Calendar.getInstance().apply { time = this@toCustomFormat }

  return when {
    thisCalendar.isSameDay(today) -> "Today"
    thisCalendar.isSameDay(yesterday) -> "Yesterday"
    else -> {
      val format = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
      format.format(this)
    }
  }
}

fun Date.toOnlyTimeFormat(): String {

  val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
  return format.format(this)
}

fun Calendar.isSameDay(other: Calendar): Boolean {
  return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
      this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateListItems(messages: List<Message>): List<ListItem> {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
  val distinctDates = messages.map { dateFormat.format(it.timestamp.toDate()) }.distinct()
  val todayDate = dateFormat.format(Date())
  val result = mutableListOf<ListItem>()

  for (date in distinctDates) {
    if (date == todayDate) {
      result.add((ListItem.DateView("Today")))
      Log.e("Dateee", "today : ${todayDate}")
    } else if (date == dateFormat.format(getYesterday(1))) {
      result.add((ListItem.DateView("Yesterday")))
      Log.e("Dateee", "yesterday : ${dateFormat.format(getYesterday(1))}")
    } else if (date == dateFormat.format(getYesterday(2))) {
      result.add((ListItem.DateView(dayFormat.format(getYesterday(2)))))
      Log.e("Dateee", "2 : ${dateFormat.format(getYesterday(2))}")
    } else if (date == dateFormat.format(getYesterday(3))) {
      result.add((ListItem.DateView(dayFormat.format(getYesterday(3)))))
      Log.e("Dateee", "3 : ${dateFormat.format(getYesterday(3))}")
    } else if (date == dateFormat.format(getYesterday(4))) {
      result.add((ListItem.DateView(dayFormat.format(getYesterday(4)))))
      Log.e("Dateee", "4 : ${dateFormat.format(getYesterday(4))}")
    } else if (date == dateFormat.format(getYesterday(5))) {
      result.add((ListItem.DateView(dayFormat.format(getYesterday(5)))))
      Log.e("Dateee", "5 : ${dateFormat.format(getYesterday(5))}")
    } else if (date == dateFormat.format(getYesterday(6))) {
      result.add((ListItem.DateView(dayFormat.format(getYesterday(6)))))
      Log.e("Dateee", "6 : ${dateFormat.format(getYesterday(6))}")
    } else {
      result.add((date?.let { ListItem.DateView(it) }!!))
    }

    val messagesForDate = messages.filter { dateFormat.format(it.timestamp.toDate()) == date }
    result.addAll(messagesForDate.map { ListItem.Messages(it) })
  }

  return result
}

fun getYesterday(i: Int): Date {
  val calendar = Calendar.getInstance()
  calendar.add(Calendar.DAY_OF_YEAR, -i)
  return calendar.time
}
