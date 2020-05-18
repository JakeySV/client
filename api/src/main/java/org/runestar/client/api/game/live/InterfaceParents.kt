package org.runestar.client.api.game.live

import org.runestar.client.api.game.NodeHashTable
import org.runestar.client.api.game.Component
import org.runestar.client.api.game.ComponentId
import org.runestar.client.raw.CLIENT
import org.runestar.client.raw.access.XNodeHashTable
import org.runestar.client.raw.access.XInterfaceParent

object InterfaceParents : NodeHashTable<ComponentId, Int, XInterfaceParent>() {

    override val accessor: XNodeHashTable get() = CLIENT.interfaceParents

    override fun wrapKey(node: XInterfaceParent): ComponentId = ComponentId(node.key.toInt())

    override fun unwrapKey(k: ComponentId): Long = k.packed.toLong()

    override fun wrapValue(node: XInterfaceParent): Int = node.itf

    fun parentId(itf: Int): Int {
        val node = firstOrNull { it.itf == itf } ?: return -1
        return node.key.toInt()
    }

    fun parent(group: Int): Component? {
        val pid = parentId(group)
        if (pid == -1) return null
        return Components[ComponentId.getItf(pid), ComponentId.getComponent(pid)]
    }
}