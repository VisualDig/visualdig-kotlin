package io.visualdig.actions

interface ActionOnElementInterface {
    val digId: Int
    val prevQueries: List<ExecutedQuery>
}