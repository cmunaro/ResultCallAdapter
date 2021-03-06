package io.github.munez07

import okhttp3.Request
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultAdapterFactory private constructor() : CallAdapter.Factory() {
    override fun get(
        returnType: Type?,
        annotations: Array<out Annotation>?,
        retrofit: Retrofit?
    ): CallAdapter<*, *>? {
        val completeType = (returnType as? ParameterizedType)
            ?.actualTypeArguments
            ?.firstOrNull() as? ParameterizedType
            ?: return null
        if (completeType.rawType != Result::class.java) return null
        val responseType = completeType.actualTypeArguments
            .firstOrNull()
            ?: return null
        return ResultAdapter<Any>(responseType)
    }

    companion object {
        @JvmStatic
        fun create() = ResultAdapterFactory()
    }
}

class ResultAdapter<R>(private val responseType: Type) :
    CallAdapter<R, Call<Result<R>>> {
    override fun responseType(): Type = responseType

    override fun adapt(call: Call<R>): Call<Result<R>> = ResultCall(call)

    inner class ResultCall<R>(private val call: Call<R>) : Call<Result<R>> {
        override fun enqueue(callback: Callback<Result<R>>) {
            call.enqueue(object : Callback<R> {
                override fun onResponse(call: Call<R>, response: Response<R>) {
                    val result: Result<R> = if (response.isSuccessful) {
                        response.body()
                            ?.let { Result.success(it) }
                            ?: Result.failure(HttpException(response))
                    } else Result.failure(HttpException(response))
                    callback.onResponse(
                        this@ResultCall,
                        Response.success(result)
                    )
                }

                override fun onFailure(call: Call<R>, throwable: Throwable) {
                    val failureResult: Result<R> = Result.failure(throwable)
                    callback.onResponse(
                        this@ResultCall,
                        Response.success(failureResult)
                    )
                }
            })
        }

        override fun execute(): Response<Result<R>> {
            throw UnsupportedOperationException("ResultCall doesn't support execute")
        }

        override fun clone(): Call<Result<R>> = ResultCall(call.clone())

        override fun isExecuted() = call.isExecuted

        override fun cancel() = call.cancel()

        override fun isCanceled() = call.isCanceled

        override fun request(): Request = call.request()
    }
}