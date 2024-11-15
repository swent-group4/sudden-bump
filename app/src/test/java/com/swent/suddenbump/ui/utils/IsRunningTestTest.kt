package com.swent.suddenbump.ui.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class IsRunningTestTest {

  @Test
  fun isRunningTestInTestEnv() {
    val result = isRunningTest()
    assertTrue(result)
  }
}
