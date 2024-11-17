package com.swent.suddenbump.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

val BackgroundColor = Color.Black // Or reference from Colors.kt if already defined
val OptionBackgroundColor = Color.Gray // Or your chosen color

fun sectionModifier() =
    Modifier.fillMaxWidth()
        .background(OptionBackgroundColor, RoundedCornerShape(8.dp))
        .padding(8.dp)

fun screenPadding(paddingValues: androidx.compose.foundation.layout.PaddingValues) =
    Modifier.fillMaxSize().padding(paddingValues).background(BackgroundColor).padding(16.dp)

fun clickableTextModifier(backgroundColor: Color, testTag: String, onClick: () -> Unit): Modifier {
  return Modifier.fillMaxWidth()
      .padding(horizontal = 8.dp)
      .clickable { onClick() }
      .background(backgroundColor, RoundedCornerShape(8.dp))
      .padding(8.dp)
      .testTag(testTag)
}
