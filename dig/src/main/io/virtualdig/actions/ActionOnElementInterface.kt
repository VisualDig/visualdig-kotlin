package io.virtualdig.actions

import io.virtualdig.element.DigTextQuery

interface ActionOnElementInterface : UsedQueryInterface {
    val digId: Int
    override val usedQueryType: String
    override val usedTextQuery: DigTextQuery?
}
