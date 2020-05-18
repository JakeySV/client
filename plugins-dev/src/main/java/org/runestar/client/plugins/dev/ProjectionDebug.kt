package org.runestar.client.plugins.dev

import org.runestar.client.api.game.live.Canvas
import org.runestar.client.api.game.live.Mouse
import org.runestar.client.api.plugins.PluginSettings
import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.game.live.Minimap
import org.runestar.client.api.game.live.Viewport
import java.awt.Color
import java.awt.Point
import java.awt.Shape
import java.awt.geom.Ellipse2D

class ProjectionDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override fun onStart() {
        add(Canvas.repaints.subscribe { g ->
            val mousePt = Mouse.location
            val fromViewportPos = Viewport.toGame(mousePt)
            if (fromViewportPos != null && fromViewportPos.isLoaded) {
                g.color = Color.RED
                g.draw(fromViewportPos.sceneTile.outline())
                val fromViewportPt = fromViewportPos.toScreen()
                if (fromViewportPt != null) {
                    g.fill(shapeAt(fromViewportPt))
                }

                val toMinimapPt = Minimap.toScreen(fromViewportPos) ?: return@subscribe
                g.color = Color.GREEN
                g.fill(shapeAt(toMinimapPt))
            }
            val fromMinimapPos = Minimap.toGame(mousePt) ?: return@subscribe
            if (fromMinimapPos.isLoaded) {
                g.color = Color.BLUE
                g.draw(fromMinimapPos.sceneTile.outline())
                val fromMinimapPt = fromMinimapPos.toScreen()
                if (fromMinimapPt != null) {
                    g.fill(shapeAt(fromMinimapPt))
                }
            }
        })
    }

    private fun shapeAt(point: Point): Shape {
        val circle = Ellipse2D.Double()
        circle.setFrameFromCenter(point, Point(point.x + 5, point.y + 5))
        return circle
    }
}