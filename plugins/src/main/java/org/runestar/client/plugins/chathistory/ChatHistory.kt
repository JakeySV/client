package org.runestar.client.plugins.chathistory

import org.runestar.client.api.plugins.DisposablePlugin
import org.runestar.client.raw.CLIENT
import org.runestar.client.raw.access.XClient
import org.runestar.client.raw.access.XIterableDualNodeQueue
import org.runestar.client.raw.access.XIterableNodeHashTable
import org.runestar.client.api.plugins.PluginSettings

class ChatHistory : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    override val name = "Chat History"

    override fun onStart() {
        add(XIterableNodeHashTable.clear.enter.subscribe {
            if (it.instance == CLIENT.messages_hashTable) it.skipBody = true
        })

        add(XIterableDualNodeQueue.clear.enter.subscribe {
            if (it.instance == CLIENT.messages_queue) it.skipBody = true
        })

        lateinit var channels: Map<*, *>
        var messageCount = 0
        add(XClient.doCycleLoggedOut.enter.subscribe {
            channels = HashMap(CLIENT.messages_channels)
            messageCount = CLIENT.messages_count
        })
        add(XClient.doCycleLoggedOut.exit.subscribe {
            CLIENT.messages_channels.putAll(channels)
            CLIENT.messages_count = messageCount
        })
    }
}