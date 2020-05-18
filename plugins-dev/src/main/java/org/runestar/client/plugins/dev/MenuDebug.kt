package org.runestar.client.plugins.dev

import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.Fonts
import org.runestar.client.api.game.live.Canvas
import org.runestar.client.api.game.live.MiniMenu
import org.runestar.client.api.plugins.PluginSettings
import java.awt.Color

class MenuDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override fun onStart() {
        add(Canvas.repaints.subscribe { g ->
            val x = 5
            var y = 40
            g.font = Fonts.PLAIN_12
            g.color = Color.WHITE
            val strings = ArrayList<String>()

            strings.apply {
                add("menu")
                add("isOpen: ${MiniMenu.isOpen}")
                add("shape: ${MiniMenu.shape}")
                add("optionsCount: ${MiniMenu.optionsCount}")
                add("options:")
                MiniMenu.options.mapTo(this) { it.toString() }
            }

            strings.forEach { s ->
                g.drawString(s, x, y)
                y += g.font.size + 5
            }

            MiniMenu.optionShapes.forEach { g.draw(it) }
        })

        add(MiniMenu.openings.subscribe { pt ->
            logger.info("Menu opened at $pt")
        })

        add(MiniMenu.actions.subscribe { a ->
            logger.info(a.toString())
        })
    }
}