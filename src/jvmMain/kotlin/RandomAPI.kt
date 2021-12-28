import retrofit2.http.GET
import retrofit2.http.Query

interface RandomAPI {
    @GET("api/v1.0/random")
    suspend fun getRandomResult(
        @Query("min") fromNumber: Int,
        @Query("max") toNumber: Int,
        @Query("count") numbersOfResults: Int,
    ): Result<List<Int>>

    @GET("api/v1.0/notExistingEndpoint")
    suspend fun brokenAPIResult(): Result<List<Int>>
}