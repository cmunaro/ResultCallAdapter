import Network.networkService
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val errorObject: List<Int>? = networkService.brokenAPIResult()
        .onFailure { println("brokenAPIResult failed with: $it") }
        .getOrNull()
    println("Error object: $errorObject")

    val successObject: List<Int>? = networkService.getRandomResult(
        fromNumber = 1,
        toNumber = 3,
        numbersOfResults = 5
    )
        .onSuccess { println("getRandomResult completed: $it") }
        .getOrNull()
    println("Success object: $successObject")
}