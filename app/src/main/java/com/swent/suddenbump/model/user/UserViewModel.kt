package com.swent.suddenbump.model.user

import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.image.ImageBitMapIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val logTag = "UserViewModel"
    private val profilePicture = ImageBitMapIO()
    val friendsLocations = mutableStateOf<Map<User, Location?>>(emptyMap())

    private val _user: MutableStateFlow<User> =
        MutableStateFlow(
            User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch")
        )
    private val _userFriendRequests: MutableStateFlow<List<User>> =
        MutableStateFlow(listOf(_user.value))
    private val _sentFriendRequests: MutableStateFlow<List<User>> =
        MutableStateFlow(listOf(_user.value))
    private val _userFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
    private val _recommendedFriends: MutableStateFlow<List<User>> =
        MutableStateFlow(listOf(_user.value))
    private val _blockedFriends: MutableStateFlow<List<User>> = MutableStateFlow(listOf(_user.value))
    private val _userLocation: MutableStateFlow<Location> =
        MutableStateFlow(
            Location("mock_provider").apply {
                latitude = 0.0
                longitude = 0.0
            }
        )
    private val _userProfilePictureChanging: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _selectedContact: MutableStateFlow<User> = MutableStateFlow(_user.value)
    private val _notificationsEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> get() = _notificationsEnabled.asStateFlow()

    private val _isLocationSharingEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLocationSharingEnabled: StateFlow<Boolean> get() = _isLocationSharingEnabled.asStateFlow()

    init {
        repository.init { Log.i(logTag, "Repository successfully initialized!") }
        loadLocationSharingStatus()
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val firestore = Firebase.firestore
                    val repository = UserRepositoryFirestore(firestore)
                    return UserViewModel(repository) as T
                }
            }
    }

    // New method to set notification preference
    fun setNotificationPreference(userId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationPreference(
                userId,
                isEnabled,
                onSuccess = { _notificationsEnabled.value = isEnabled },
                onFailure = { exception -> Log.e(logTag, "Failed to set notification preference: ${exception.message}") }
            )
        }
    }

    // New method to load notification preference
    fun loadNotificationPreference(userId: String) {
        viewModelScope.launch {
            repository.getNotificationPreference(
                userId,
                onSuccess = { isEnabled -> _notificationsEnabled.value = isEnabled },
                onFailure = { exception -> Log.e(logTag, "Failed to load notification preference: ${exception.message}") }
            )
        }
    }

    fun setCurrentUser() {
        repository.getUserAccount(
            onSuccess = {
                _user.value = it
                repository.getUserFriends(
                    it,
                    onSuccess = { friends -> _userFriends.value = friends },
                    onFailure = { e -> Log.e(logTag, e.toString()) }
                )
            },
            onFailure = { e -> Log.e(logTag, e.toString()) })
    }

    fun setCurrentUser(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUserAccount(
            uid,
            onSuccess = {
                _user.value = it
                repository.getUserFriends(
                    it,
                    onSuccess = { friends -> _userFriends.value = friends },
                    onFailure)
            },
            onFailure)
    }

    fun setUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        _user.value = user
        repository.updateUserAccount(user, onSuccess, onFailure)
    }

    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = _user.value.uid
        repository.updateUsername(userId, newUsername, onSuccess, onFailure)
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

    fun acceptFriendRequest(
        user: User = _user.value,
        friend: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.createFriend(user, friend, onSuccess, onFailure)
        repository.createFriend(friend, user, onSuccess, onFailure)
        _sentFriendRequests.value = _sentFriendRequests.value.minus(friend)
        _userFriendRequests.value = _userFriendRequests.value.plus(friend)
    }

    fun sendFriendRequest(
        user: User = _user.value,
        friend: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.createFriendRequest(user, friend, onSuccess, onFailure)
        _sentFriendRequests.value = _sentFriendRequests.value.plus(friend)
    }

    fun getCurrentUser(): StateFlow<User> {
        return _user.asStateFlow()
    }

    fun getUserFriends(): StateFlow<List<User>> {
        return _userFriends.asStateFlow()
    }

    fun getUserFriendRequests(): StateFlow<List<User>> {
        return _userFriendRequests.asStateFlow()
    }

    fun setUserFriendRequests(
        user: User = _user.value,
        friendRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        _userFriendRequests.value = friendRequestsList
        repository.setUserFriendRequests(user, friendRequestsList, onSuccess, onFailure)
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

    fun getUserRecommendedFriends(): StateFlow<List<User>> {
        return _recommendedFriends.asStateFlow()
    }

    fun getSentFriendRequests(): StateFlow<List<User>> {
        return _sentFriendRequests.asStateFlow()
    }

    fun setSentFriendRequests(
        user: User = _user.value,
        sentRequestsList: List<User>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        _sentFriendRequests.value = sentRequestsList
        repository.setSentFriendRequests(user, sentRequestsList, onSuccess, onFailure)
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

    fun loadFriendsLocations() {
        repository.getFriendsLocation(
            _user.value,
            onSuccess = { friendsLoc ->
                // Update the state with the locations of friends
                friendsLocations.value = friendsLoc
            },
            onFailure = { error ->
                // Handle the error, e.g., log or show error message
                Log.e("UserViewModel", "Failed to load friends' locations: ${error.message}")
            })
    }

    fun getSelectedContact(): StateFlow<User> {
        return _selectedContact.asStateFlow()
    }

    fun setSelectedContact(user: User) {
        _selectedContact.value = user
    }

    fun getRelativeDistance(friend: User): Float {
        loadFriendsLocations()
        val userLocation = _userLocation.value
        val friendLocation = friendsLocations.value[friend]
        return if (friendLocation != null) {
            userLocation.distanceTo(friendLocation)
        } else {
            Float.MAX_VALUE
        }
    }

    fun getNewUid(): String {
        return repository.getNewUid()
    }

    private fun loadLocationSharingStatus() {
        viewModelScope.launch {
            _isLocationSharingEnabled.value = repository.getLocationSharingStatus()
        }
    }

    fun updateLocationSharingStatus(enabled: Boolean) {
        viewModelScope.launch {
            repository.setLocationSharingStatus(enabled)
            _isLocationSharingEnabled.value = enabled
        }
    }
}