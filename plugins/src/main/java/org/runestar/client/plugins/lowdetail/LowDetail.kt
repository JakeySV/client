package org.runestar.client.plugins.lowdetail

import org.runestar.client.api.game.live.Scene
import org.runestar.client.raw.CLIENT
import org.runestar.client.api.plugins.AbstractPlugin
import org.runestar.client.api.plugins.PluginSettings

class LowDetail : AbstractPlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override val name = "Low Detail"

    override fun start() {
        setLowDetail(true)
    }

    override fun stop() {
        setLowDetail(false)
    }

    private fun setLowDetail(lowDetail: Boolean) {
        CLIENT.isLowDetail = lowDetail
        CLIENT.scene_isLowDetail = lowDetail
        Scene.reload()
    }
}