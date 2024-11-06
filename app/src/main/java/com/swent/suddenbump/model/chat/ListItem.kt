package com.swent.suddenbump.model.chat

sealed class ListItem {
  data class Messages(val message: Message) : ListItem()

  data class DateView(val date: String) : ListItem()
}
