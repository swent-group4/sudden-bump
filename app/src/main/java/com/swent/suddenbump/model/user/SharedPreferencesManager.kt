package com.swent.suddenbump.model.user

import android.content.Context

/**
 * A manager class for handling shared preferences in the application.
 *
 * @property context The context used to access the shared preferences.
 */
class SharedPreferencesManager(private val context: Context) {
  private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  /**
   * Saves a string value in the shared preferences.
   *
   * @param key The key under which the value is stored.
   * @param value The string value to be stored.
   */
  fun saveString(key: String, value: String) {
    sharedPreferences.edit().putString(key, value).apply()
  }

  /**
   * Saves a boolean value in the shared preferences.
   *
   * @param key The key under which the value is stored.
   * @param value The boolean value to be stored.
   */
  fun saveBoolean(key: String, value: Boolean) {
    sharedPreferences.edit().putBoolean(key, value).apply()
  }

  /**
   * Retrieves a string value from the shared preferences.
   *
   * @param key The key under which the value is stored.
   * @param defaultValue The default value to return if the key does not exist.
   * @return The string value associated with the key, or the default value if the key does not
   *   exist.
   */
  fun getString(key: String, defaultValue: String = ""): String {
    return sharedPreferences.getString(key, defaultValue) ?: defaultValue
  }

  /**
   * Retrieves a boolean value from the shared preferences.
   *
   * @param key The key under which the value is stored.
   * @param defaultValue The default value to return if the key does not exist.
   * @return The boolean value associated with the key, or the default value if the key does not
   *   exist.
   */
  fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return sharedPreferences.getBoolean(key, defaultValue)
  }

  /** Clears all values stored in the shared preferences. */
  fun clearPreferences() {
    sharedPreferences.edit().clear().apply()
  }
}
