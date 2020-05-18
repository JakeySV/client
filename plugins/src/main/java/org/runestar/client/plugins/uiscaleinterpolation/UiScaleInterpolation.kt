package org.runestar.client.plugins.uiscaleinterpolation

import org.runestar.client.raw.access.XRasterProvider
import org.runestar.client.api.plugins.PluginSettings
import org.runestar.client.api.plugins.DisposablePlugin
import java.awt.Graphics2D
import java.awt.RenderingHints

class UiScaleInterpolation : DisposablePlugin<UiScaleInterpolation.Settings>() {

    override val defaultSettings = Settings()

    override val name = "UI Scale Interpolation"

    override fun onStart() {
        val interpolationValue = settings.interpolation.renderingHintValue
        add(XRasterProvider.drawFull0.enter.subscribe { e ->
            val g = e.arguments[0] as Graphics2D
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationValue)
        })
    }

    enum class Interpolation(val renderingHintValue: Any) {
        NEAREST_NEIGHBOR(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR),
        BILINEAR(RenderingHints.VALUE_INTERPOLATION_BILINEAR),
        BICUBIC(RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    }

    data class Settings(
            val interpolation: Interpolation = Interpolation.BILINEAR
    ) : PluginSettings()
}