package com.swent.suddenbump.model.user

import android.content.Context

class SharedPreferencesManager(private val context: Context) {
  private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  fun saveString(key: String, value: String) {
    sharedPreferences.edit().putString(key, value).apply()
  }

  fun saveBoolean(key: String, value: Boolean) {
    sharedPreferences.edit().putBoolean(key, value).apply()
  }

  fun getString(key: String, defaultValue: String = ""): String {
    return sharedPreferences.getString(key, defaultValue) ?: defaultValue
  }

  fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return sharedPreferences.getBoolean(key, defaultValue)
  }

  fun clearPreferences() {
    sharedPreferences.edit().clear().apply()
  }
}
