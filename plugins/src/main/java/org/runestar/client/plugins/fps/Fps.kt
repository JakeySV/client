package org.runestar.client.plugins.fps

import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.util.drawStringShadowed
import org.runestar.client.api.Fonts
import org.runestar.client.api.game.live.Canvas
import org.runestar.client.raw.CLIENT
import org.runestar.client.api.plugins.PluginSettings
import java.awt.Color

class Fps : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override val name = "FPS"

    override fun onStart() {
        add(Canvas.repaints.subscribe { g ->
            g.color = Color.YELLOW
            g.font = Fonts.PLAIN_12

            g.drawStringShadowed(
                    CLIENT.fps.toString(),
                    CLIENT.canvasWidth - 43,
                    g.fontMetrics.ascent + 1
            )
        })
    }
}