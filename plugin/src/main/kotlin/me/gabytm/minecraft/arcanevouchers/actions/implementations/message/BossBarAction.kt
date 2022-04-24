package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.functions.sync
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Ticks
import org.bukkit.entity.Player

class BossBarAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val broadcast: Broadcast = Broadcast.parse(meta.properties[BROADCAST])
    private val color: BossBar.Color = meta.getProperty(COLOR, DEFAULT_COLOR) {
        BossBar.Color.NAMES.value(it.lowercase())
    }
    private val flags: Set<BossBar.Flag> = meta.getProperty(FLAGS, emptySet()) { property ->
        property.split(Constant.Separator.COMMA)
            .mapNotNull { BossBar.Flag.NAMES.value(it.lowercase()) }
            .toSet()
    }
    private val overlay: BossBar.Overlay = meta.getProperty(OVERLAY, DEFAULT_OVERLAY) {
        BossBar.Overlay.NAMES.value(it.lowercase())
    }
    private val progress: Float = meta.getProperty(PROGRESS, DEFAULT_PROGRESS) { it.toFloatOrNull() }
    private val duration: Long = meta.getProperty(DURATION, DEFAULT_DURATION) { it.toLongOrNull() }

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

    @Suppress("unused", "SpellCheckingInspection")
    companion object {

        private const val BROADCAST: String = "broadcast"
        private const val COLOR: String = "color"
        private const val FLAGS: String = "flags"
        private const val OVERLAY: String = "overlay"
        private const val PROGRESS: String = "progress"
        private const val DURATION: String = "duration"

        private val DEFAULT_COLOR: BossBar.Color = BossBar.Color.WHITE
        private val DEFAULT_OVERLAY: BossBar.Overlay = BossBar.Overlay.PROGRESS
        private const val DEFAULT_PROGRESS: Float = BossBar.MAX_PROGRESS
        private const val DEFAULT_DURATION: Long = (Ticks.TICKS_PER_SECOND * 10).toLong()

        private const val ID: String = "bossbar"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Display a boss bar on player's screen"))
            // Optional properties
            .property(COLOR) {
                type(UsageBuilder.STRING)
                    .description("the name of a boss bar Color")
                    .default(DEFAULT_COLOR.name)
            }
            .property(FLAGS) {
                type(UsageBuilder.LIST)
                    .description("command separated of boss bar Flag names")
                    .default("none")
            }
            .property(OVERLAY) {
                type(UsageBuilder.STRING)
                    .description("the name of a boss bar Overlay")
                    .default(DEFAULT_OVERLAY.name)
            }
            .property(PROGRESS) {
                type(UsageBuilder.FLOAT)
                    .description("the progress of the bar")
                    .default("MAX_PROGRESS ($DEFAULT_PROGRESS)")
            }
            .property(DURATION) {
                type(UsageBuilder.FLOAT)
                    .description("how long the bar will be displayed")
                    .default("$DEFAULT_DURATION (${DEFAULT_DURATION / Ticks.TICKS_PER_SECOND} seconds")
            }
            // Required arguments
            .argument("message") {
                type(UsageBuilder.STRING)
                    .description("the message that will be displayed")
                    .required()
            }
            .build()

    }

}