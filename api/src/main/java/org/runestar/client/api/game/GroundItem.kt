package org.runestar.client.api.game

import org.runestar.client.raw.access.XObj

class GroundItem(
        override val accessor: XObj,
        override val modelPosition: Position
) : Entity(accessor) {

    override val orientation get() = Angle.ZERO

    val id get() = accessor.id

    val quantity get() = accessor.quantity

    val item get() = Item(id, quantity)

    override fun toString(): String {
        return "GroundItem(item=$item)"
    }

    companion object {

        const val MAX_QUANTITY = 65535
    }
}