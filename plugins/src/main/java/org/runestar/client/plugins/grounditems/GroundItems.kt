package org.runestar.client.plugins.grounditems

import org.runestar.client.api.Fonts
import org.runestar.client.api.forms.FontForm
import org.runestar.client.api.forms.RegexForm
import org.runestar.client.api.forms.RgbaForm
import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.util.drawStringShadowed
import org.runestar.client.api.game.GroundItem
import org.runestar.client.api.game.SceneElement
import org.runestar.client.api.game.live.Game
import org.runestar.client.api.game.live.Canvas
import org.runestar.client.api.game.live.Scene
import org.runestar.client.api.game.live.Viewport
import org.runestar.client.api.game.live.SceneElements
import org.runestar.client.raw.CLIENT
import org.runestar.client.raw.access.XObjType
import org.runestar.client.api.plugins.PluginSettings

class GroundItems : DisposablePlugin<GroundItems.Settings>() {

    override val defaultSettings = Settings()

    override val name = "Ground Items"

    val piles = LinkedHashSet<SceneElement.ObjStack>()

    val blockedIds = HashSet<Int>()
    val unblockedIds = HashSet<Int>()
    val blockRegexes = ArrayList<Regex>()

    override fun onStart() {
        settings.blockedNames.mapTo(blockRegexes) { it.value }

        add(SceneElements.ObjStack.added.subscribe { piles.add(it) })
        add(SceneElements.ObjStack.removed.subscribe { piles.remove(it) })
        add(SceneElements.cleared.subscribe { piles.clear() })
        Scene.reload()

        val defaultColor = settings.color.value
        val font = settings.font.value

        add(Canvas.repaints.subscribe { g ->
            g.font = font
            g.clip(Viewport.shape)
            val height = g.fontMetrics.height

            val itr = piles.iterator()
            while (itr.hasNext()) {
                val pile = itr.next()
                if (pile.plane != Game.plane) continue
                val pt = pile.modelPosition.toScreen()
                if (pt == null || pt !in g.clip) continue
                val items = pile.toList().asReversed()
                val x = pt.x
                var y = pt.y - settings.initialOffset
                items.forEach { item ->
                    val def = CLIENT.getObjType(item.id)
                    val count = item.quantity

                    if (isBlocked(def, count)) {
                        return@forEach
                    }
                    val string = itemToString(def, count)
                    val width = g.fontMetrics.stringWidth(string)
                    val leftX = x - (width / 2)

                    g.color = defaultColor
                    g.drawStringShadowed(string, leftX, y)

                    y -= height + settings.spacing
                }
            }
        })
    }

    fun itemToString(def: XObjType, count: Int): String {
        val name = def.name
        return when {
            count == 1 -> name
            count >= GroundItem.MAX_QUANTITY -> "$name x Lots!"
            else -> "$name x $count"
        }
    }

    fun isBlocked(def: XObjType, count: Int): Boolean {
        val id = def.id
        val name = def.name
        return if (id in blockedIds) {
            true
        } else if (id in unblockedIds) {
            false
        } else {
            val blocked = blockRegexes.any { it.matches(name) }
            if (blocked) {
                blockedIds.add(id)
                true
            } else {
                unblockedIds.add(id)
                false
            }
        }
    }

    override fun onStop() {
        piles.clear()
        blockedIds.clear()
        unblockedIds.clear()
        blockRegexes.clear()
    }

    data class Settings(
            val color: RgbaForm = RgbaForm(255, 255, 255),
            val font: FontForm = FontForm(Fonts.PLAIN_11),
            val spacing: Int = 0,
            val initialOffset: Int = 9,
            val blockedNames: List<RegexForm> = listOf(RegexForm("Vial"))
    ) : PluginSettings()
}