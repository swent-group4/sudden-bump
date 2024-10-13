package com.swent.suddenbump.model.user

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import com.swent.suddenbump.model.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel(private val repository: UserRepository) {

  private val logTag = "UserViewModel"

  private val _user: MutableStateFlow<User> =
      MutableStateFlow(
          User(
              "1",
              "Martin",
              "Vetterli",
              Icons.Outlined.AccountCircle,
              "+41 00 000 00 01",
              "martin.vetterli@epfl.ch"))
  private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _userLocation: MutableStateFlow<Location> = MutableStateFlow(Location(0.0, 0.0))

  init {
    repository.init { Log.i(logTag, "Repository successfully initialized!") }
  }

  fun setCurrentUser() {
    repository.getUserAccount(
        onSuccess = { _user.value = it }, onFailure = { Log.e(logTag, it.toString()) })
    repository.getUserFriends(
        user = _user.value,
        onSuccess = { _userFriends.value = it },
        onFailure = { Log.e(logTag, it.toString()) })
    repository.getBlockedFriends(
        user = _user.value,
        onSuccess = { _blockedFriends.value = it },
        onFailure = { Log.e(logTag, it.toString()) })
  }

  fun verifyNoAccountExists(
      emailAddress: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.verifyNoAccountExists(emailAddress, onSuccess, onFailure)
  }

  fun createUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.createUserAccount(user, onSuccess, onFailure)
  }

  fun getCurrentUser(): StateFlow<User> {
    return _user.asStateFlow()
  }

  fun getUserFriends(): StateFlow<List<User>> {
    return _userFriends.asStateFlow()
  }

  fun setUserFriends(
      user: User = _user.value,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _userFriends.value = friendsList
    repository.setUserFriends(user, friendsList, onSuccess, onFailure)
  }

  fun getBlockedFriends(): StateFlow<List<User>> {
    return _blockedFriends.asStateFlow()
  }

  fun setBlockedFriends(
      user: User = _user.value,
      blockedFriendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _blockedFriends.value = blockedFriendsList
    repository.setBlockedFriends(user, blockedFriendsList, onSuccess, onFailure)
  }

  fun updateLocation(
      user: User = _user.value,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _userLocation.value = location
    repository.updateLocation(user, location, onSuccess, onFailure)
  }

  fun getLocation(): StateFlow<Location> {
    return _userLocation.asStateFlow()
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }
}
