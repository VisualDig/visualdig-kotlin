package io.visualdig.results

data class SpacialSearchResult(override val result: Result,
                               val message: String,
                               val digId: Int?,
                               val closeResults : List<CloseResult>)
    : TestResultInterface


data class CloseResult(val x: Int,
                       val y: Int,
                       val tolerance: Int,
                       val htmlId : String)