package org.runestar.client.api.game.live

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.runestar.client.api.game.ChunkTemplate
import org.runestar.client.api.game.ClanChat
import org.runestar.client.api.game.FriendsSystem
import org.runestar.client.api.game.GlobalTile
import org.runestar.client.api.game.HintArrow
import org.runestar.client.api.game.LocalValue
import org.runestar.client.api.game.Position
import org.runestar.client.api.game.SceneTile
import org.runestar.client.api.game.VarpId
import org.runestar.client.api.util.ObservableExecutor
import org.runestar.client.raw.CLIENT
import org.runestar.client.raw.access.XClient

object Game {

    val state: Int get() = CLIENT.gameState

    val stateChanges: Observable<Int> = XClient.updateGameState.exit.map { it.arguments[0] as Int }

    val ticks: Observable<Unit> = XClient.updateNpcs.exit
            .map { Unit }
            .delay { XClient.doCycle.enter }
            .share()

    val cycle get() = CLIENT.cycle

    val plane get() = CLIENT.plane

    val runEnergy get() = CLIENT.runEnergy

    val weight get() = CLIENT.weight

    val windowMode: Int get() = CLIENT.clientPreferences.windowMode

    val clanChat: ClanChat? get() = CLIENT.clanChat?.let { ClanChat(it) }

    val friendsSystem: FriendsSystem get() = FriendsSystem(CLIENT.friendSystem)

    val specialAttackEnabled get() = Vars.getVarp(VarpId.SPECIAL_ATTACK_ENABLED) != 0

    /**
     * 0 - 100
     */
    val specialAttackPercent get() = Vars.getVarp(VarpId.SPECIAL_ATTACK_PERCENT) / 10

    val destination: SceneTile? get() {
        val x = CLIENT.destinationX
        val y = CLIENT.destinationY
        if (x == 0 && y == 0) return null
        return SceneTile(x, y, plane)
    }

    val selectedTile: SceneTile? get() {
        val x = CLIENT.scene_selectedX
        if (x == -1) return null
        return SceneTile(x, CLIENT.scene_selectedY, plane)
    }

    val executor = ObservableExecutor(XClient.doCycle.exit)

    val scheduler = Schedulers.from(executor)

    val hintArrow: HintArrow? get() = when (CLIENT.hintArrowType) {
        1 -> Npcs[CLIENT.hintArrowNpcIndex]?.let { HintArrow.OnNpc(it) }
        2 -> HintArrow.Static(
                Position(
                        LocalValue(CLIENT.hintArrowX - CLIENT.baseX, CLIENT.hintArrowSubX).value,
                        LocalValue(CLIENT.hintArrowY - CLIENT.baseY, CLIENT.hintArrowSubY).value,
                        CLIENT.hintArrowHeight * 2,
                        CLIENT.plane
                )
        )
        10 -> Players[CLIENT.hintArrowPlayerIndex]?.let { HintArrow.OnPlayer(it) }
        else -> null
    }

    private fun instanceChunkTemplate(tile: SceneTile): ChunkTemplate {
        return ChunkTemplate(CLIENT.instanceChunkTemplates[tile.plane][tile.x / ChunkTemplate.CHUNK_SIZE][tile.y / ChunkTemplate.CHUNK_SIZE])
    }

    fun toTemplate(sceneTile: SceneTile): GlobalTile {
        if (!CLIENT.isInInstance) return sceneTile.toGlobalTile()
        val template = instanceChunkTemplate(sceneTile)
        val chunkX = sceneTile.x and (ChunkTemplate.CHUNK_SIZE - 1)
        val chunkY = sceneTile.y and (ChunkTemplate.CHUNK_SIZE - 1)
        val rotation = (4 - template.rotation) % 4
        return GlobalTile(
                template.x * ChunkTemplate.CHUNK_SIZE + ChunkTemplate.rotateX(chunkX, chunkY, rotation),
                template.y * ChunkTemplate.CHUNK_SIZE + ChunkTemplate.rotateY(chunkX, chunkY, rotation),
                template.plane
        )
    }

    fun fromTemplate(globalTile: GlobalTile): List<SceneTile> {
        if (!CLIENT.isInInstance) return listOf(globalTile.toSceneTile())
        val tiles = ArrayList<SceneTile>()
        CLIENT.instanceChunkTemplates.forEachIndexed { plane, xs ->
            xs.forEachIndexed { x, ys ->
                ys.forEachIndexed t@{ y, t ->
                    if (t == -1) return@t
                    val template = ChunkTemplate(t)
                    if (template.plane != globalTile.plane) return@t
                    val dx = globalTile.x - (template.x * ChunkTemplate.CHUNK_SIZE)
                    if (dx !in 0..(ChunkTemplate.CHUNK_SIZE - 1)) return@t
                    val dy = globalTile.y - (template.y * ChunkTemplate.CHUNK_SIZE)
                    if (dy !in 0..(ChunkTemplate.CHUNK_SIZE - 1)) return@t
                    val rotation = template.rotation
                    val tile = SceneTile(
                            x * ChunkTemplate.CHUNK_SIZE + ChunkTemplate.rotateX(dx, dy, rotation),
                            y * ChunkTemplate.CHUNK_SIZE + ChunkTemplate.rotateY(dx, dy, rotation),
                            plane
                    )
                    tiles.add(tile)
                }
            }
        }
        return tiles
    }
}