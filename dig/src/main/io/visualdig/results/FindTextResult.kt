package io.visualdig.results

data class FindTextResult(override val result: Result,
                          val digId: Int?,
                          val htmlId: String?,
                          val closestMatches : List<String>) : TestResultInterface
