package io.visualdig.results

enum class Result(val result: String) {
    Failure_NoMatch("Failure_NoMatch"),
    Failure_AmbiguousMatch("Failure_AmbiguousMatch"),
    Failure_QueryExpired("Failure_QueryExpired"),
    Success("Success")
}

fun Result.isFailure() : Boolean {
    return this.result.startsWith("Failure")
}
fun Result.isSuccess() : Boolean {
    return this == Result.Success
}

data class TestResult(override val result: Result,
                      val message: String) : TestResultInterface

interface TestResultInterface {
    val result : Result
}