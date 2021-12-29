# ResultCallAdapter
A safe way to handle retrofit results and exceptions.

**ResultAdapter** encapsulate Retrofit2's results inside a Kotlin `Result`. In that way Retrofit2 can't make your application crash from unhandled exceptions.

Kotlin [Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) is a useful data structure that help us handle errors. An easy example:
```
  val result: Result<Int> = runCatching { unsafeMethodReturningAnInt() }
  val value = result
    .onSuccess { it: Int -> println("Sbeam!") }
    .onFailure { it: Throwable -> println("Thrown: $it") }
    .getOrNull()
```

## How to use
1. Add the `ResultAdapter` among your other Retrofit build configurations
```
    val retrofit = Retrofit.Builder()
        .baseUrl("http://your.magic.domain/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .addCallAdapterFactory(ResultAdapterFactory.create())    // That one
        .build()
```

2. Define your APIs
```
    @GET("someEndpoint")
    suspend fun getSomething(): Result<SomethingSpecial>
```

3. Enjoy your safe API calls
```    
    val somethingSpecial: SomethingSpecial? = networkService.getSomething().getOrNull()
```
