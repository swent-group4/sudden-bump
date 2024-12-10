package com.swent.suddenbump.model.meeting_location

import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class NominatimLocationRepositoryTest {

    @Mock private lateinit var mockClient: OkHttpClient
    @Mock private lateinit var mockCall: Call

    private lateinit var repository: NominatimLocationRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = NominatimLocationRepository(mockClient)

        `when`(mockClient.newCall(org.mockito.kotlin.any())).thenReturn(mockCall)
    }

    @Test
    fun `search should return parsed locations on successful response`() {
        val jsonResponse = """
            [
                {"display_name": "Location 1", "lat": "12.34", "lon": "56.78"},
                {"display_name": "Location 2", "lat": "98.76", "lon": "54.32"}
            ]
        """
        val mockResponse = createMockResponse(200, jsonResponse)

        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            callback.onResponse(mockCall, mockResponse)
            null
        }.`when`(mockCall).enqueue(org.mockito.kotlin.any())

        var result: List<Location>? = null
        var error: Exception? = null

        repository.search("query", { result = it }, { error = it })

        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals("Location 1", result!![0].name)
        assertEquals(12.34, result!![0].latitude)
        assertEquals(56.78, result!![0].longitude)
    }

    @Test
    fun `search should handle network failure`() {
        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            callback.onFailure(mockCall, IOException("Network error"))
            null
        }.`when`(mockCall).enqueue(org.mockito.kotlin.any())

        var result: List<Location>? = null
        var error: Exception? = null

        repository.search("query", { result = it }, { error = it })

        assertNull(result)
        assertNotNull(error)
        assertTrue(error is IOException)
        assertEquals("Network error", error!!.message)
    }

    @Test
    fun `search should handle invalid JSON response`() {
        val invalidJson = "INVALID_JSON"
        val mockResponse = createMockResponse(200, invalidJson)

        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            callback.onResponse(mockCall, mockResponse)
            null
        }.`when`(mockCall).enqueue(org.mockito.kotlin.any())

        var result: List<Location>? = null
        var error: Exception? = null

        repository.search("query", { result = it }, { error = it })

        assertNotNull(result)
        assertTrue(result!!.isEmpty())
        assertNull(error)
    }

    @Test
    fun `search should handle HTTP error`() {
        val mockResponse = createMockResponse(404, "")

        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            callback.onResponse(mockCall, mockResponse)
            null
        }.`when`(mockCall).enqueue(org.mockito.kotlin.any())

        var result: List<Location>? = null
        var error: Exception? = null

        repository.search("query", { result = it }, { error = it })

        assertNull(result)
        assertNotNull(error)
        assertTrue(error is IOException)
        assertEquals("Unexpected HTTP code 404", error!!.message)
    }

    private fun createMockResponse(code: Int, body: String): Response {
        return Response.Builder()
            .code(code)
            .protocol(Protocol.HTTP_1_1)
            .message("")
            .request(Request.Builder().url("http://localhost").build())
            .body(ResponseBody.create("application/json".toMediaTypeOrNull(), body))
            .build()
    }
}
