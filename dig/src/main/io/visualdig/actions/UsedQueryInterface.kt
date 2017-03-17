package io.visualdig.actions

import io.visualdig.element.DigTextQuery

interface UsedQueryInterface {
    val usedQueryType : String
    val usedTextQuery : DigTextQuery?
}