package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.functions.sync
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.util.Ticks
import org.bukkit.entity.Player

class BossBarAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val broadcast: Broadcast = Broadcast.parse(meta.properties["broadcast"])
    private val color: BossBar.Color = meta.getProperty("color", BossBar.Color.WHITE) {
        BossBar.Color.NAMES.value(it.lowercase())
    }
    private val flags: Set<BossBar.Flag> = meta.getProperty("flags", emptySet()) { property ->
        property.split(Constant.Separator.COMMA)
            .mapNotNull { BossBar.Flag.NAMES.value(it.lowercase()) }
            .toSet()
    }
    private val overlay: BossBar.Overlay = meta.getProperty("overlay", BossBar.Overlay.PROGRESS) {
        BossBar.Overlay.NAMES.value(it.lowercase())
    }
    private val progress: Float = meta.getProperty("progress", BossBar.MAX_PROGRESS) { it.toFloatOrNull() }
    private val duration: Long = meta.getProperty("duration", (Ticks.TICKS_PER_SECOND * 10).toLong()) { it.toLongOrNull() }

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val name = meta.getParsedData(player, context).mini()
            val bossBar = BossBar.bossBar(name, progress, color, overlay, flags)
            val players = broadcast.getPlayers(player)

            players.forEach { it.audience().showBossBar(bossBar) }
            sync(duration) {
                players
                    .filter { it.isOnline }
                    .forEach { it.audience().hideBossBar(bossBar) }
            }
        }
    }

}