package io.virtualdig.actions

import io.virtualdig.element.DigTextQuery

interface UsedQueryInterface {
    val usedQueryType : String
    val usedTextQuery : DigTextQuery?
}