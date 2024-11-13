package com.swent.suddenbump.model.user

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.image.ImageBitMapIO
import com.swent.suddenbump.worker.WorkerScheduler.scheduleLocationUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

  private val logTag = "UserViewModel"
  private val profilePicture = ImageBitMapIO()
  val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

  private val _user: MutableStateFlow<User> =
      MutableStateFlow(
          User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch"))
  private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
  private val _userLocation: MutableStateFlow<Location> =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })
  private val _userProfilePictureChanging: MutableStateFlow<Boolean> = MutableStateFlow(false)

  init {
    repository.init { Log.i(logTag, "Repository successfully initialized!") }
  }

  companion object {
    fun provideFactory(context: Context): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val repository = UserRepositoryFirestore(Firebase.firestore, context)
          return UserViewModel(repository) as T
        }
      }
    }
  }

  fun setCurrentUser() {
    repository.getUserAccount(
        onSuccess = { user ->
          _user.value = user
          saveUserLoginStatus(_user.value.uid)
          scheduleLocationUpdateWorker(getApplicationContext(), _user.value.uid)
          Log.d(logTag, "User set 1: ${_user.value}")
          repository.getUserFriends(
              user = _user.value,
              onSuccess = { friendsList ->
                Log.d(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    user = _user.value,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
        },
        onFailure = { e -> Log.e(logTag, e.toString()) })
  }

  fun setCurrentUser(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserAccount(
        uid,
        onSuccess = {
          _user.value = it
          repository.getUserFriends(
              user = _user.value,
              onSuccess = { friendsList ->
                Log.i(logTag, friendsList.toString())
                _userFriends.value = friendsList
                repository.getBlockedFriends(
                    user = _user.value,
                    onSuccess = { blockedFriendsList ->
                      _blockedFriends.value = blockedFriendsList
                      onSuccess()
                    },
                    onFailure = { e -> Log.e(logTag, e.toString()) })
              },
              onFailure = { e -> Log.e(logTag, e.toString()) })
        },
        onFailure)
  }

  fun setUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
    repository.updateUserAccount(user, onSuccess, onFailure)
  }

  /** onSuccess returns TRUE if no account exists */
  fun verifyNoAccountExists(
      emailAddress: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.verifyNoAccountExists(emailAddress, onSuccess, onFailure)
  }

  fun createUserAccount(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    _user.value = user
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

  fun getLocation(): StateFlow<Location> {
    return _userLocation.asStateFlow()
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

  fun updateTimestamp(
      user: User = _user.value,
      timestamp: Timestamp,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.updateTimestamp(user, timestamp, onSuccess, onFailure)
  }

  fun loadFriendsLocations() {
    viewModelScope.launch {
      try {
        Log.i(logTag, "1: ${_userFriends.value.toString()}")
        Log.i(logTag, "2: ${getUserFriends().value.toString()}")
        repository.getFriendsLocation(
            _userFriends.value,
            onSuccess = { friendsLoc ->
              // Update the state with the locations of friends
              friendsLocations.value = friendsLoc
              Log.d("FriendsMarkers", "On success load Friends Locations ${friendsLocations.value}")
            },
            onFailure = { error ->
              // Handle the error, e.g., log or show error message
              Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
            })
      } catch (e: Exception) {
        Log.e("UserViewModel", e.toString())
      }
    }
  }

  fun getRelativeDistance(friend: User): Float {
    loadFriendsLocations()
    val userLocation = _userLocation.value
    val friendLocation = friendsLocations.value.get(friend)
    return if (friendLocation != null) {
      userLocation.distanceTo(friendLocation)
    } else {
      Float.MAX_VALUE
    }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun saveUserLoginStatus(userId: String) {
    repository.saveLoginStatus(userId)
  }

  fun getSavedUid(): String {
    return repository.getSavedUid()
  }

  fun isUserLoggedIn(): Boolean {
    return repository.isUserLoggedIn()
  }

  fun logout() {
    repository.logoutUser()
  }
}
