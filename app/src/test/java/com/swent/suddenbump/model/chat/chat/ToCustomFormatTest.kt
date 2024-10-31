package com.swent.suddenbump.model.chat.chat

import com.swent.suddenbump.model.chat.toCustomFormat
import java.util.Calendar
import java.util.Date
import kotlin.test.assertEquals
import org.junit.Test

class ToCustomFormatTest {
  @Test
  fun testToCustomFormatToday() {
    val today = Date()
    assertEquals("Today", today.toCustomFormat())
  }

  @Test
  fun testToCustomFormatYesterday() {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterday = calendar.time
    assertEquals("Yesterday", yesterday.toCustomFormat())
  }
}
