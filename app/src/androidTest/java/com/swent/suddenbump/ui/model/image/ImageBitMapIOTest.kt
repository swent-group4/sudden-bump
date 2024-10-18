package com.swent.suddenbump.ui.model.image

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.swent.suddenbump.model.image.ImageBitMapIO
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageBitMapIOTest {

  private lateinit var context: Context

  private val imageBitMapIO = ImageBitMapIO()
  private val path = "/pictures/test.jpg"
  private val imageBitMap = ImageBitmap(1, 1)

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()

    imageBitMapIO.setInternalImageBitMap(context, path, imageBitMap, {}, {})
  }

  @Test
  fun setInternalImageBitMap() {
    imageBitMapIO.setInternalImageBitMap(context, path, imageBitMap, {}, {})
  }
}
