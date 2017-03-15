package io.virtualdig.results

data class FindTextResult(override val result: Result,
                          val digId: Int?,
                          val closestMatches : List<String>) : TestResultInterface
