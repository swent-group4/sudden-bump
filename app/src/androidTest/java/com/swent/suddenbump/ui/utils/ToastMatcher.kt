package com.swent.suddenbump.ui.utils

import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root?>() {

  override fun describeTo(description: Description?) {
    description?.appendText("is toast")
  }

  override fun matchesSafely(item: Root?): Boolean {
    val type: Int? = item?.windowLayoutParams?.get()?.type
    Log.d("ToastMatcher", "Type: $type")
    if (type == WindowManager.LayoutParams.TYPE_TOAST ||
        type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
      val windowToken: IBinder = item.decorView.windowToken
      val appToken: IBinder = item.decorView.getApplicationWindowToken()
      if (windowToken === appToken) { // means this window isn't contained by any other windows.
        Log.d("ToastMatcher", "Toast detected")
        return true
      }
    }
    Log.d("ToastMatcher", "Not a toast")
    return false
  }
}
