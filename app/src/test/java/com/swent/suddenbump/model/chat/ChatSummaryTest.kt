package com.swent.suddenbump.model.chat

import android.location.Location
import com.swent.suddenbump.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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

  private val unknownUser =
      User(
          uid = "unknown",
          firstName = "Unknown",
          lastName = "User",
          phoneNumber = "+33 0 00 00 00 00",
          null,
          "mail@mail.com",
          MutableStateFlow(
              Location("provider").apply {
                latitude = 0.0
                longitude = 0.0
              }))

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

  @Test
  fun convertParticipantsUidToDisplayWorksWithTwoUsers() {
    val chatSummary = ChatSummary(participants = users.map { it.uid })

    val result = convertParticipantsUidToDisplay(chatSummary, userDummy1, users, unknownUser)
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

    val result =
        convertParticipantsUidToDisplay(chatSummary, userDummy1, usersModified, unknownUser)
    assertEquals("Martin Vetterli, Martine Veto early", result)
  }

  @Test
  fun convertLastSenderUidToDisplayWorksWithTwoUsers() {
    val chatSummary = ChatSummary(lastMessageSenderId = userDummy1.uid)

    val result = convertLastSenderUidToDisplay(chatSummary, userDummy1, users, unknownUser)
    assertEquals("You", result)
  }

  @Test
  fun convertLastSenderUidToDisplayWorksWithMultipleUsers() {
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
    val chatSummary = ChatSummary(lastMessageSenderId = userDummy2.uid)

    val result = convertLastSenderUidToDisplay(chatSummary, userDummy1, usersModified, unknownUser)
    assertEquals("Martin Vetterli", result)

    val usersModified2 = listOf(userDummy1, userDummy2, userDummy3)
    val chatSummary2 = ChatSummary(lastMessageSenderId = userDummy3.uid)

    val result2 =
        convertLastSenderUidToDisplay(chatSummary2, userDummy1, usersModified2, unknownUser)
    assertEquals("Martine Veto early", result2)
  }

  @Test
  fun convertFirstParticipantToUserWorksWithTwoUsers() {
    val userDummy3 =
        User(
            "3",
            "Martine",
            "Veto early",
            "+41 00 000 00 01",
            null,
            "martin.vetterli@epfl.ch",
            locationDummy)

    val usersModified = listOf(userDummy1, userDummy3)
    val chatSummary = ChatSummary(participants = usersModified.map { it.uid })

    val result = convertFirstParticipantToUser(chatSummary, usersModified, unknownUser)
    assertEquals(userDummy1.uid, result.uid)
  }
}
