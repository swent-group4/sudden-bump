package com.swent.suddenbump.model.user

import android.content.Context
import android.content.SharedPreferences
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class SharedPreferencesManagerTest {

  private lateinit var context: Context
  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var editor: SharedPreferences.Editor
  private lateinit var sharedPreferencesManager: SharedPreferencesManager

  @Before
  fun setUp() {
    // Mock Context, SharedPreferences, and Editor
    context = mock(Context::class.java)
    sharedPreferences = mock(SharedPreferences::class.java)
    editor = mock(SharedPreferences.Editor::class.java)

    `when`(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
        .thenReturn(sharedPreferences)
    `when`(sharedPreferences.edit()).thenReturn(editor)

    // Mock Editor methods to return itself for chaining
    `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
    `when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)
    `when`(editor.clear()).thenReturn(editor)

    sharedPreferencesManager = SharedPreferencesManager(context)
  }

  @Test
  fun saveStringStoresAStringInSharedPreferences() {
    val key = "key_string"
    val value = "value_string"

    sharedPreferencesManager.saveString(key, value)

    verify(editor).putString(key, value)
    verify(editor).apply()
  }

  @Test
  fun saveBooleanStoresABooleanInSharedPreferences() {
    val key = "key_boolean"
    val value = true

    sharedPreferencesManager.saveBoolean(key, value)

    verify(editor).putBoolean(key, value)
    verify(editor).apply()
  }

  @Test
  fun getStringRetrievesAStringFromSharedPreferences() {
    val key = "key_string"
    val defaultValue = "default_value"
    val storedValue = "stored_value"

    `when`(sharedPreferences.getString(key, defaultValue)).thenReturn(storedValue)

    val result = sharedPreferencesManager.getString(key, defaultValue)

    assertEquals(storedValue, result)
  }

  @Test
  fun getStringReturnsDefaultValueWhenKeyIsNotFound() {
    val key = "non_existent_key"
    val defaultValue = "default_value"

    `when`(sharedPreferences.getString(key, defaultValue)).thenReturn(null)

    val result = sharedPreferencesManager.getString(key, defaultValue)

    assertEquals(defaultValue, result)
  }

  @Test
  fun getBooleanRetrievesABooleanFromSharedPreferences() {
    val key = "key_boolean"
    val defaultValue = false
    val storedValue = true

    `when`(sharedPreferences.getBoolean(key, defaultValue)).thenReturn(storedValue)

    val result = sharedPreferencesManager.getBoolean(key, defaultValue)

    assertEquals(storedValue, result)
  }

  @Test
  fun getBooleanReturnsDefaultValueWhenKeyIsNotFound() {
    val key = "non_existent_key"
    val defaultValue = false

    `when`(sharedPreferences.getBoolean(key, defaultValue)).thenReturn(defaultValue)

    val result = sharedPreferencesManager.getBoolean(key, defaultValue)

    assertEquals(defaultValue, result)
  }

  @Test
  fun clearPreferencesClearsAllSharedPreferences() {
    sharedPreferencesManager.clearPreferences()

    verify(editor).clear()
    verify(editor).apply()
  }
}
