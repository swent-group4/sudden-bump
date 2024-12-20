package com.swent.suddenbump.ui.utils

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.MainActivity
import kotlin.test.assertFalse
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IsInternetAvailableTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
  private lateinit var context: Context

  @Before
  fun setUp() {
    composeTestRule.waitForIdle()

    context = composeTestRule.activity.applicationContext
  }

  @After
  fun tearDown() {
    isMockUsingOfflineMode = false

    isMockUsingOnlineDefaultValue = false
    testableOnlineDefaultValue = false
  }

  @Test
  fun isInternetAvailableWorks() {
    val result = isInternetAvailable(context)
    assertTrue(result)
  }

  @Test
  fun isInternetAvailableWorksWithMockOfflineMode() {
    isMockUsingOfflineMode = true

    val result = isInternetAvailable(context)
    assertFalse(result)
  }

  @Test
  fun isInternetAvailableWorksWithMockUsingOnlineDefaultValue() {
    isMockUsingOnlineDefaultValue = true

    testableOnlineDefaultValue = true
    val result1 = isInternetAvailable(context)
    assertTrue(result1)

    testableOnlineDefaultValue = false
    val result2 = isInternetAvailable(context)
    assertFalse(result2)
  }
}
