package com.swent.suddenbump.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.theme.Pinkish
import com.swent.suddenbump.ui.theme.Purple40

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(60.dp).testTag("bottomNavigationMenu"),
      containerColor = Color.Black) {
        tabList.forEach { tab ->
          val tabScreen = tab.route + " Screen"
          val isSelected = tabScreen == selectedItem
          NavigationBarItem(
              icon = {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (isSelected) Purple40 else Color.White)
              },
              label = { Text(text = tab.textId, color = if (isSelected) Pinkish else Color.White) },
              selected = isSelected,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(RoundedCornerShape(50.dp)).testTag(tab.textId))
        }
      }
}
