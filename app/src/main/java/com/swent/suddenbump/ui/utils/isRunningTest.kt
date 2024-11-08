package com.swent.suddenbump.ui.utils

import java.util.concurrent.atomic.AtomicBoolean

private var isRunningTest: AtomicBoolean? = null

@Synchronized
fun isRunningTest(): Boolean {
  if (null == isRunningTest) {
    var istest: Boolean
    try {
      // "android.support.test.espresso.Espresso" if you haven't migrated to androidx yet
      Class.forName("androidx.test.espresso.Espresso")
      istest = true
    } catch (e: ClassNotFoundException) {
      istest = false
    }
    isRunningTest = AtomicBoolean(istest)
  }
  return isRunningTest!!.get()
}
