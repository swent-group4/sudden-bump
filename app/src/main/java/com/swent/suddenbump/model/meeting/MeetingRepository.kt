package com.swent.suddenbump.model.meeting

interface MeetingRepository {

  /**
   * Generates a new unique meeting ID.
   *
   * @return A new unique meeting ID as a String.
   */
  fun getNewMeetingId(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess Callback function to be called upon successful initialization.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Fetches the list of meetings.
   *
   * @param onSuccess Callback function to be called with the list of meetings upon successful
   *   fetch.
   * @param onFailure Callback function to be called with an exception if the fetch fails.
   */
  fun getMeetings(onSuccess: (List<Meeting>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a new meeting to the repository.
   *
   * @param meeting The meeting to be added.
   * @param onSuccess Callback function to be called upon successful addition.
   * @param onFailure Callback function to be called with an exception if the addition fails.
   */
  fun addMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing meeting in the repository.
   *
   * @param meeting The meeting to be updated.
   * @param onSuccess Callback function to be called upon successful update.
   * @param onFailure Callback function to be called with an exception if the update fails.
   */
  fun updateMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a meeting from the repository by its ID.
   *
   * @param id The ID of the meeting to be deleted.
   * @param onSuccess Callback function to be called upon successful deletion.
   * @param onFailure Callback function to be called with an exception if the deletion fails.
   */
  fun deleteMeetingById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
