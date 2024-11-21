package com.swent.suddenbump.model.chat

import android.location.Location
import com.google.firebase.firestore.*
import com.swent.suddenbump.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ChatSummaryTest {

  private val message = "sixoutofsixgrade"
  private val participants = listOf("uidUser1", "uidUser2")

  private val locationDummy =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0 // Set latitude
            longitude = 0.0 // Set longitude
          })

  private val userDummy1 =
      User(
          "1",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          null,
          "martin.vetterli@epfl.ch",
          locationDummy)

  private val userDummy2 =
      User(
          "2",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          null,
          "martin.vetterli@epfl.ch",
          locationDummy)

  private val users = listOf(userDummy1, userDummy2)

  @Before fun setUp() {}

  @After fun tearDown() {}

  @Test
  fun senderWorksWithOneUser() {
    val chatSummary = ChatSummary(participants = listOf("uidUser1"))

    assertEquals("uidUser1", chatSummary.sender)
  }

  @Test
  fun senderWorksWithTwoUsers() {
    val chatSummary = ChatSummary(participants = participants)

    assertEquals("uidUser1, uidUser2", chatSummary.sender)
  }

  @Test
  fun contentWorksWithNonEmptyMessage() {
    val chatSummary = ChatSummary(lastMessage = message, participants = participants)

    assertEquals(message, chatSummary.content)
  }

  @Test
  fun dateReturnsEmptyStringWhenNull() {
    val chatSummary = ChatSummary(participants = participants)

    assertEquals("", chatSummary.date)
  }

  //  @Test
  //  fun dateWorksWithNonNullDate() {
  //    val timestamp = Timestamp(seconds = 1729515283, nanoseconds = 0)
  //    val chatSummary = ChatSummary(participants = participants, lastMessageTimestamp = timestamp)
  //
  //    val timeZone = TimeZone.getTimeZone("UTC") // Set your desired time zone
  //    val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)
  //    dateFormat.timeZone = timeZone
  //
  //    val date = chatSummary.date
  //    assertEquals(
  //        dateFormat.format(dateFormat.parse("10/21/2024 02:54 PM")!!),
  //        dateFormat.format(dateFormat.parse(chatSummary.date)!!))
  //  }

  @Test
  fun convertParticipantsUidToDisplayWorksWithTwoUsers() {
    val chatSummary = ChatSummary(participants = users.map { it.uid })

    val result = convertParticipantsUidToDisplay(chatSummary, userDummy1, users)
    assertEquals("Martin Vetterli", result)
  }

  @Test
  fun convertParticipantsUidToDisplayWorksWithMultipleUsers() {
    val userDummy3 =
        User(
            "3",
            "Martine",
            "Veto early",
            "+41 00 000 00 01",
            null,
            "martin.vetterli@epfl.ch",
            locationDummy)

    val usersModified = listOf(userDummy1, userDummy2, userDummy3)
    val chatSummary = ChatSummary(participants = usersModified.map { it.uid })

    val result = convertParticipantsUidToDisplay(chatSummary, userDummy1, usersModified)
    assertEquals("Martin Vetterli, Martine Veto early", result)
  }
}
