package org.runestar.client.plugins.dev

import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.Fonts
import org.runestar.client.api.game.live.Canvas
import org.runestar.client.api.game.live.Mouse
import org.runestar.client.api.plugins.PluginSettings
import java.awt.Color

class MouseDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override fun onStart() {
        add(Canvas.repaints.subscribe { g ->
            val x = 5
            var y = 40
            g.font = Fonts.PLAIN_12
            g.color = Color.WHITE
            val strings = ArrayList<String>()

            strings.apply {
                add("mouse")
                add("location: ${Mouse.location}")
                add("viewportLocation: ${Mouse.viewportLocation}")
                add("isInViewport: ${Mouse.isInViewport}")
                add("entityCount: ${Mouse.entityCount}")
                add("tags:")
            }

            strings.forEach { s ->
                g.drawString(s, x, y)
                y += g.font.size + 5
            }
        })
    }
}