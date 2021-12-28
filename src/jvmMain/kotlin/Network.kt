import com.google.gson.GsonBuilder
import network.ResultAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Network {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://www.randomnumberapi.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .addCallAdapterFactory(ResultAdapterFactory.create())
        .build()

    val networkService: RandomAPI = retrofit.create(RandomAPI::class.java)
}