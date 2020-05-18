package org.runestar.client.plugins.dev

import com.google.common.base.Splitter
import org.kxtra.slf4j.info
import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.api.game.live.Canvas
import org.runestar.client.raw.CLIENT
import org.runestar.client.api.plugins.PluginSettings

class VarpsChangeDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    val varps = CLIENT.varps_main

    @Volatile
    var old = varps.copyOf()

    override fun onStart() {
        add(Canvas.repaints.subscribe { g ->
            val curr = varps.copyOf()
            for (i in old.indices) {
                val o = old[i]
                val c = curr[i]
                if (o != c) {
                    val changedBits = Integer.toBinaryString(o xor c).padStart(Integer.SIZE, '0')
                    val lastBitChanged = Integer.SIZE - changedBits.lastIndexOf('1') - 1
                    val firstBitChanged = Integer.SIZE - changedBits.indexOf('1') - 1

                    logger.info { "$i: $firstBitChanged - $lastBitChanged\n" +
                            "${intToString(o)} ->\n" +
                            intToString(c)
                    }
                }
            }

            old = curr
        })
    }

    val splitter8 = Splitter.fixedLength(8)

    fun intToString(n: Int): String {
        val b = Integer.toBinaryString(n).padStart(Integer.SIZE, '0')
        return splitter8.splitToList(b).joinToString("'")
    }
}