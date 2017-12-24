package org.runestar.client.plugins.std.debug

import org.runestar.client.game.api.live.LiveCanvas
import org.runestar.client.game.raw.Client
import org.runestar.client.plugins.PluginSettings
import org.runestar.client.plugins.utils.ColorForm
import org.runestar.client.plugins.utils.DisposablePlugin
import org.runestar.client.plugins.utils.FontForm
import java.awt.Font

class ClientPreferencesDebug : DisposablePlugin<ClientPreferencesDebug.Settings>() {

    override val defaultSettings = Settings()

    override fun start() {
        super.start()
        add(LiveCanvas.repaints.subscribe { g ->
            val p = Client.accessor.clientPreferences ?: return@subscribe
            val strings = listOf(
                    "titleMusicDisabled: ${p.titleMusicDisabled}",
                    "roofsHidden: ${p.roofsHidden}",
                    "hideUsername: ${p.hideUsername}",
                    "windowMode: ${p.windowMode}",
                    "rememberedUsername: ${p.rememberedUsername}",
                    "parameters: ${p.parameters}"
            )
            g.font = settings.font.get()
            g.color = settings.color.get()
            val x = 20
            var y = 40
            strings.forEach { s ->
                g.drawString(s, x, y)
                y += g.font.size + 5
            }
        })
    }

    class Settings : PluginSettings() {
        val font = FontForm(Font.SANS_SERIF, FontForm.BOLD, 15f)
        val color = ColorForm()
    }
}