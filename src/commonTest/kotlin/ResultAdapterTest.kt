import com.google.common.truth.Truth.assertThat
import com.google.gson.reflect.TypeToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import network.ResultAdapter
import network.ResultAdapterFactory
import okhttp3.Request
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import java.lang.reflect.WildcardType

class ResultAdapterTest {
    @get:Rule
    val server: MockWebServer = MockWebServer()

    private val bodyClassString =
        object : TypeToken<Call<Result<String>>>() {}.type
    @Suppress("UNCHECKED_CAST")
    private val callAdapter = ResultAdapterFactory.create()
        .get(bodyClassString, null, null) as ResultAdapter<String>
    private val stubRequest = Request.Builder()
        .url("http://stub")
        .build()
    private lateinit var resultCall: Call<Result<String>>
    private val call = mockk<Call<String>>()

    @Before
    fun setUp() {
        every { call.clone() } returns mockk<Call<String>>()
        every { call.isExecuted } returns true
        every { call.cancel() } returns Unit
        every { call.isCanceled } returns true
        every { call.request() } returns stubRequest

        resultCall = callAdapter.adapt(call)
    }

    @Test
    fun `whenever call responseType then return the owned responseType`() {
        val type = callAdapter.responseType()
        val upperBounds = (type as WildcardType).upperBounds
        val responseType = upperBounds.first()

        assertThat(upperBounds.size).isEqualTo(1)
        assertThat(responseType).isEqualTo(String::class.java)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `execute is not supported`() {
        resultCall.execute()
    }

    @Test
    fun `whenever call clone then return an identical instance`() {
        val newInstance = resultCall.clone()

        assertThat(newInstance).isNotSameInstanceAs(resultCall)
        verify { call.clone() }
    }

    @Test
    fun `whenever call isExecuted then return the ask to the Call`() {
        val result = resultCall.isExecuted

        assertThat(result).isTrue()
        verify { call.isExecuted }
    }

    @Test
    fun `whenever call cancel then call cancel in the Call`() {
        resultCall.cancel()

        verify { call.cancel() }
    }

    @Test
    fun `whenever call isCanceled then return the ask to the Call`() {
        val result = resultCall.isCanceled

        assertThat(result).isTrue()
        verify { call.isCanceled }
    }

    @Test
    fun `whenever call request then return the Call it in the Call`() {
        val result = resultCall.request()

        assertThat(result).isSameInstanceAs(stubRequest)
        verify { call.request() }
    }
}