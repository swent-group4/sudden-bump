package com.swent.suddenbump.model.meeting

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MeetingRepositoryFirestoreTest {

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockDocumentReference: DocumentReference
    @Mock private lateinit var mockCollectionReference: CollectionReference
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock private lateinit var mockMeetingQuerySnapshot: QuerySnapshot

    private lateinit var meetingRepositoryFirestore: MeetingRepositoryFirestore

    private val meeting =
        Meeting(
            meetingId = "1",
            location = "Cafe",
            date = Timestamp.now(),
            friendId = "friendId",
            creatorId = "creatorId")

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Initialize Firebase if necessary
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }

        meetingRepositoryFirestore = MeetingRepositoryFirestore(mockFirestore)

        `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
        `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    }

    @Test
    fun getNewMeetingId() {
        `when`(mockDocumentReference.id).thenReturn("1")
        val meetingId = meetingRepositoryFirestore.getNewMeetingId()
        assert(meetingId == "1")
    }

    @Test
    fun getMeetings_callsDocuments() {
        `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockMeetingQuerySnapshot))
        `when`(mockMeetingQuerySnapshot.documents).thenReturn(listOf())

        meetingRepositoryFirestore.getMeetings(
            onSuccess = {
                // Do nothing; we just want to verify that the 'documents' field was accessed
            },
            onFailure = { fail("Failure callback should not be called") })

        verify(timeout(100)) { (mockMeetingQuerySnapshot).documents }
    }

    @Test
    fun addMeeting_shouldCallFirestoreCollection() {
        `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

        meetingRepositoryFirestore.addMeeting(meeting, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).set(any())
    }

    @Test
    fun deleteMeetingById_shouldCallDocumentReferenceDelete() {
        `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        meetingRepositoryFirestore.deleteMeetingById("1", onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

        verify(mockDocumentReference).delete()
    }
}