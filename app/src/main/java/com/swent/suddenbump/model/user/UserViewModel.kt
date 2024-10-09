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
      MutableStateFlow(User("", "", "", Icons.Outlined.AccountCircle, "", ""))
  private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
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

  fun getUser(): StateFlow<User> {
    return _user.asStateFlow()
  }

  fun getUserFriends(user: User = _user.value): StateFlow<List<User>> {
    repository.getUserFriends(
        user = user,
        onSuccess = { _userFriends.value = it },
        onFailure = { Log.e(logTag, it.toString()) })
    return _userFriends.asStateFlow()
  }

  fun setUserFriends(
      user: User = _user.value,
      friendsList: List<User>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.setUserFriends(user, friendsList, onSuccess, onFailure)
  }

  fun getBlockedFriends(): StateFlow<List<User>> {
    return _blockedFriends.asStateFlow()
  }

  fun getLocation(): StateFlow<Location> {
    return _userLocation.asStateFlow()
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }
}
