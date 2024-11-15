package com.swent.suddenbump.ui.utils

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
