package io.github.munez07

import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.WildcardType

class ResultAdapterFactoryTest {
    @get:Rule
    val server: MockWebServer = MockWebServer()

    private val factory: ResultAdapterFactory = ResultAdapterFactory.create()
    private val annotations = arrayOf<Annotation>()
    private var retrofit: Retrofit? = null

    @Before
    fun setUp() {
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .addCallAdapterFactory(factory)
            .build()
    }

    @Test
    fun `whenever a non Result returnType is requested then return null`() {
        val bodyClassString = object : TypeToken<Call<String>>() {}
        assertThat(
            factory.get(bodyClassString.type, annotations, retrofit)
                ?.responseType()
        ).isNull()
        val bodyClassNonResult =
            object : TypeToken<Call<List<String>>>() {}
        assertThat(
            factory.get(bodyClassNonResult.type, annotations, retrofit)
                ?.responseType()
        ).isNull()
    }

    @Test
    fun `when a Result returnType is requested then parse its type`() {
        val bodyClassResult =
            object : TypeToken<Call<Result<List<String>>>>() {}
        val expectedResponseResponse = object : TypeToken<List<String>>() {}
        assertThat(
            (factory.get(bodyClassResult.type, annotations, retrofit)
                ?.responseType() as? WildcardType)
                ?.upperBounds?.first()
        ).isEqualTo(expectedResponseResponse.type)
    }
}