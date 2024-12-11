package com.swent.suddenbump.ui.utils

import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel

@Synchronized
fun isRunningTest(): Boolean {
  return try {
    // Attempt to load the class without initializing it to avoid issues
    Class.forName(
        "androidx.test.espresso.Espresso", false, Thread.currentThread().contextClassLoader)
    true
  } catch (e: ClassNotFoundException) {
    false
  } catch (e: NoClassDefFoundError) {
    false
  }
}

var isUsingMockViewModel = false
lateinit var testableMeetingViewModel: MeetingViewModel
lateinit var testableUserViewModel: UserViewModel
