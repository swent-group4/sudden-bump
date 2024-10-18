package com.swent.suddenbump.ui.model.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.swent.suddenbump.model.image.ImageRepositoryFirebaseStorage
import com.swent.suddenbump.model.user.UserRepositoryFirestoreHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageRepositoryFirebaseStorageTest {

  private lateinit var firebaseStorage: FirebaseStorage
  private lateinit var imageRepositoryFirebaseStorage: ImageRepositoryFirebaseStorage
  private lateinit var storageReference: StorageReference

  private val uid: String = "X20fPnbl3p9KIMavYbkS.jpeg"
  private val helper = UserRepositoryFirestoreHelper()

  @Before
  fun setUp() {

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    firebaseStorage = Firebase.storage("gs://sudden-bump-swent.appspot.com")
    imageRepositoryFirebaseStorage = ImageRepositoryFirebaseStorage(firebaseStorage)
    storageReference = firebaseStorage.reference.child("profilePictures")
  }

  @Test
  fun downloadImage() {
    var imageBitmap: ImageBitmap = ImageBitmap(1, 1)
    imageRepositoryFirebaseStorage.downloadImage(
        helper.uidToProfilePicturePath(uid, storageReference), { imageBitmap = it }, {})
  }

  @Test
  fun uploadImage() {
    var imageBitmap: ImageBitmap = ImageBitmap(1, 1)
    imageRepositoryFirebaseStorage.uploadImage(
        imageBitmap, helper.uidToProfilePicturePath(uid, storageReference), {}, {})
  }
}
