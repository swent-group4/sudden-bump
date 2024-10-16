package com.swent.suddenbump.model

import com.swent.suddenbump.model.location.Location
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserViewModelTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  private val exception = Exception()
  private val location = Location(0.0, 0.0)
  private val user =
      User(
          "1",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          "martin.vetterli@epfl.ch")

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)
  }

  @Test
  fun setCurrentUser() {
    val user2 =
        User(
            "2222",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            "martin.vetterli@epfl.ch")

    doAnswer { invocationOnMock ->
          val onSuccess = invocationOnMock.getArgument<(User) -> Unit>(0)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(1)
          onSuccess(user2)
          onFailure(exception)
        }
        .whenever(userRepository)
        .getUserAccount(any(), any())

    doAnswer { invocationOnMock ->
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
          onSuccess(listOf(user2))
          onFailure(exception)
        }
        .whenever(userRepository)
        .getUserFriends(any(), any(), any())

    doAnswer { invocationOnMock ->
          val user = invocationOnMock.getArgument<User>(0)
          val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
          val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
          user
          onSuccess(listOf(user2))
          onFailure(exception)
        }
        .whenever(userRepository)
        .getBlockedFriends(any(), any(), any())

    userViewModel.setCurrentUser()

    verify(userRepository).getUserAccount(any(), any())
    verify(userRepository).getUserFriends(any(), any(), any())
    verify(userRepository).getBlockedFriends(any(), any(), any())

    assertThat(userViewModel.getCurrentUser().value.uid, `is`(user2.uid))
    assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
    assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
  }

  @Test
  fun verifyNoAccountExists() {
    userViewModel.verifyNoAccountExists(user.emailAddress, {}, {})
    verify(userRepository).verifyNoAccountExists(any(), any(), any())
  }

  @Test
  fun createUserAccount() {
    userViewModel.createUserAccount(user, {}, {})
    verify(userRepository).createUserAccount(any(), any(), any())
  }

  @Test
  fun getCurrentUser() {
    assertThat(userViewModel.getCurrentUser().value.uid, `is`(user.uid))
    assertThat(userViewModel.getCurrentUser().value.emailAddress, `is`(user.emailAddress))
    assertThat(userViewModel.getCurrentUser().value.lastName, `is`(user.lastName))
    assertThat(userViewModel.getCurrentUser().value.firstName, `is`(user.firstName))
    assertThat(userViewModel.getCurrentUser().value.phoneNumber, `is`(user.phoneNumber))
  }

  @Test
  fun getUserFriends() {
    val user2 =
        User(
            "2222",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            "martin.vetterli@epfl.ch")

    assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user.uid))
    assert(!userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
  }

  @Test
  fun setUserFriends() {
    val user2 =
        User(
            "2222",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            "martin.vetterli@epfl.ch")

    assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user.uid))

    userViewModel.setUserFriends(friendsList = listOf(user2), onSuccess = {}, onFailure = {})
    assert(userViewModel.getUserFriends().value.map { it.uid }.contains(user2.uid))
  }

  @Test
  fun getBlockedFriends() {
    val user2 =
        User(
            "2222",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            "martin.vetterli@epfl.ch")

    assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user.uid))
    assert(!userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
  }

  @Test
  fun setBlockedFriends() {
    val user2 =
        User(
            "2222",
            "Martin",
            "Vetterli",
            "+41 00 000 00 01",
            "martin.vetterli@epfl.ch")

    assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user.uid))

    userViewModel.setBlockedFriends(
        blockedFriendsList = listOf(user2), onSuccess = {}, onFailure = {})
    assert(userViewModel.getBlockedFriends().value.map { it.uid }.contains(user2.uid))
  }

  @Test
  fun getLocation() {
    val location = Location(0.0, 0.0)

    assertThat(userViewModel.getLocation().value.latitude, `is`(0.0))
    assertThat(userViewModel.getLocation().value.longitude, `is`(0.0))
  }

  @Test
  fun updateLocation() {
    val location = Location(1.0, 1.0)

    userViewModel.updateLocation(location = location, onSuccess = {}, onFailure = {})
    verify(userRepository).updateLocation(any(), any(), any(), any())
    assertThat(userViewModel.getLocation().value.latitude, `is`(1.0))
    assertThat(userViewModel.getLocation().value.longitude, `is`(1.0))
  }

  @Test
  fun getNewUid() {
    `when`(userRepository.getNewUid()).thenReturn("uid")
    assertThat(userViewModel.getNewUid(), `is`("uid"))
  }
}
