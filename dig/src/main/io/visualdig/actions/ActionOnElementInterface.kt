package io.visualdig.actions

import io.visualdig.element.DigTextQuery

interface ActionOnElementInterface : UsedQueryInterface {
    val digId: Int
    override val usedQueryType: String
    override val usedTextQuery: DigTextQuery?
}
