import com.google.common.truth.Truth.assertThat
import com.google.gson.reflect.TypeToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import network.ResultAdapter
import network.ResultAdapterFactory
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.WildcardType

class ResultAdapterTest {
    private val bodyClassString =
        object : TypeToken<Call<Result<String>>>() {}.type

    @Suppress("UNCHECKED_CAST")
    private val callAdapter = ResultAdapterFactory.create()
        .get(bodyClassString, null, null) as ResultAdapter<String>
    private val stubRequest = Request.Builder()
        .url("http://stub")
        .build()
    private lateinit var resultCall: Call<Result<String>>
    private val originalCall = mockk<Call<String>>()
    private val callbackResult = mockk<Callback<Result<String>>>()

    @Before
    fun setUp() {
        every { originalCall.clone() } returns mockk<Call<String>>()
        every { originalCall.isExecuted } returns true
        every { originalCall.cancel() } returns Unit
        every { originalCall.isCanceled } returns true
        every { originalCall.request() } returns stubRequest
        every { callbackResult.onResponse(any(), any()) } returns Unit
        every { callbackResult.onFailure(any(), any()) } returns Unit

        resultCall = callAdapter.adapt(originalCall)
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
        verify { originalCall.clone() }
    }

    @Test
    fun `whenever call isExecuted then return the ask to the Call`() {
        val result = resultCall.isExecuted

        assertThat(result).isTrue()
        verify { originalCall.isExecuted }
    }

    @Test
    fun `whenever call cancel then call cancel in the Call`() {
        resultCall.cancel()

        verify { originalCall.cancel() }
    }

    @Test
    fun `whenever call isCanceled then return the ask to the Call`() {
        val result = resultCall.isCanceled

        assertThat(result).isTrue()
        verify { originalCall.isCanceled }
    }

    @Test
    fun `whenever call request then return the Call it in the Call`() {
        val result = resultCall.request()

        assertThat(result).isSameInstanceAs(stubRequest)
        verify { originalCall.request() }
    }

    @Test
    fun `whenever get a valid response return it in a Result Success`() {
        every { originalCall.enqueue(any()) } answers {
            firstArg<Callback<String>>().onResponse(
                originalCall,
                Response.success("hello world")
            )
        }

        resultCall.enqueue(callbackResult)

        verify {
            callbackResult.onResponse(
                any(),
                withArg<Response<Result<String>>> {
                    assertThat(it.body()?.isFailure).isFalse()
                    assertThat(it.body()).isEqualTo(Result.success("hello world"))
                }
            )
        }
    }

    @Test
    fun `whenever get a null response return it in a Result Failure`() {
        every { originalCall.enqueue(any()) } answers {
            firstArg<Callback<String>>().onResponse(
                originalCall,
                Response.success(null)
            )
        }

        resultCall.enqueue(callbackResult)

        verify {
            callbackResult.onResponse(
                any(),
                withArg<Response<Result<String>>> {
                    assertThat(it.body()?.isFailure).isTrue()
                }
            )
        }
    }

    @Test
    fun `whenever get a unsuccessful response return it in a Result Failure`() {
        val responseBody = mockk<ResponseBody>()
        val mediaType = mockk<MediaType>()
        every { responseBody.contentType() } returns mediaType
        every { responseBody.contentLength() } returns 0
        every { originalCall.enqueue(any()) } answers {
            firstArg<Callback<String>>().onResponse(
                originalCall,
                Response.error(404, responseBody)
            )
        }

        resultCall.enqueue(callbackResult)

        verify {
            callbackResult.onResponse(
                any(),
                withArg<Response<Result<String>>> {
                    assertThat(it.body()?.isFailure).isTrue()
                }
            )
        }
    }

    @Test
    fun `whenever get a failure return it in a Result Failure`() {
        val responseBody = mockk<ResponseBody>()
        val mediaType = mockk<MediaType>()
        every { responseBody.contentType() } returns mediaType
        every { responseBody.contentLength() } returns 0
        every { originalCall.enqueue(any()) } answers {
            firstArg<Callback<String>>().onFailure(
                originalCall,
                HttpException(Response.error<String>(500, responseBody))
            )
        }

        resultCall.enqueue(callbackResult)

        verify {
            callbackResult.onResponse(
                any(),
                withArg<Response<Result<String>>> {
                    assertThat(it.body()?.isFailure).isTrue()
                }
            )
        }
    }
}