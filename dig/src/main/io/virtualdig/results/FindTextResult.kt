package io.virtualdig.results

data class FindTextResult(override val result: Result,
                          val closestMatches : List<String>) : TestResultInterface
