package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import com.google.common.base.Enums
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.entity.Player
import java.time.Duration

class MessageAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val messageType: MessageType = meta.properties["type"]?.let { MessageType.find(it) } ?: MessageType.CHAT
    private val broadcast: Broadcast = Broadcast.parse(meta.properties["broadcast"])

    //
    private val times: Title.Times = Title.Times.of(
        parseDuration("fadeIn", Title.DEFAULT_TIMES.fadeIn()),
        parseDuration("stay", Title.DEFAULT_TIMES.stay()),
        parseDuration("fadeOut", Title.DEFAULT_TIMES.fadeOut())
    )

    private fun parseDuration(key: String, default: Duration): Duration {
        return meta.properties[key]?.toLongOrNull()?.let { Ticks.duration(it) } ?: default
    }

    private fun send() {}

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            if (messageType == MessageType.TITLE) {
                val parts = meta.getParsedData(player, context).split(Constant.NEW_LINE_SEPARATOR)
                val title = Title.title(parts[0].mini(), parts[1].mini())
                broadcast.broadcast(player) { it.showTitle(title) }
                return@execute
            }

            val message = meta.getParsedData(player, context).mini()

            when (messageType) {
                MessageType.ACTION -> broadcast.broadcast(player) { it.sendActionBar(message) }
                MessageType.CHAT -> broadcast.broadcast(player) { it.sendMessage(message) }
            }
        }
    }

    private enum class MessageType {
        /**
         * Actionbar
         */
        ACTION,

        /**
         * Normal chat message
         */
        CHAT,

        /**
         * Title
         */
        TITLE;

        companion object {

            fun find(string: String): MessageType = Enums.getIfPresent(MessageType::class.java, string.uppercase()).or(CHAT);

        }

    }

}