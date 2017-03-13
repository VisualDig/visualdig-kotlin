package io.virtualdig.results

enum class Result(val result: String) {
    Failure("Failure"),
    Success("Success")
}

fun Result.isFailure() : Boolean {
    return this == Result.Failure
}
fun Result.isSuccess() : Boolean {
    return this == Result.Success
}

data class TestResult(override val result: Result,
                      val message: String) : TestResultInterface

interface TestResultInterface {
    val result : Result
}