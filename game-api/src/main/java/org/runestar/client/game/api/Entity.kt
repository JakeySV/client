package org.runestar.client.game.api

import org.runestar.client.game.raw.Wrapper
import org.runestar.client.game.raw.access.XEntity

abstract class Entity(override val accessor: XEntity) : Wrapper() {

    open val height get() = accessor.height

    abstract val position: Position

    abstract val orientation: Angle
}