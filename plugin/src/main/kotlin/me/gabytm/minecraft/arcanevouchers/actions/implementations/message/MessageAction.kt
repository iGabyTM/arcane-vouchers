package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.entity.Player
import java.time.Duration

class MessageAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val messageType: MessageType = meta.getProperty("type", MessageType.CHAT) { MessageType.find(it) }
    private val broadcast: Broadcast = Broadcast.parse(meta.properties["broadcast"])

    private val times: Title.Times = Title.Times.times(
        parseDuration("fadeIn", Title.DEFAULT_TIMES.fadeIn()),
        parseDuration("stay", Title.DEFAULT_TIMES.stay()),
        parseDuration("fadeOut", Title.DEFAULT_TIMES.fadeOut())
    )

    private fun parseDuration(key: String, default: Duration): Duration {
        return meta.getProperty(key, default) { it.toLongOrNull()?.let(Ticks::duration) }
    }

    override fun getName(): String {
        return if (messageType == MessageType.CHAT) {
            "Message"
        } else {
            "Message ($messageType)"
        }
    }

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            if (messageType == MessageType.TITLE) {
                val parts = meta.getParsedData(player, context).split(Constant.Separator.NEW_LINE, 2)
                val title = Title.title(
                    parts[0].mini(),
                    if (parts.size == 2) parts[1].mini() else Component.empty(),
                    times
                )
                broadcast.broadcast(player) { it.showTitle(title) }
                return@execute
            }

            val message = meta.getParsedData(player, context).mini()

            when (messageType) {
                MessageType.ACTION -> broadcast.broadcast(player) { it.sendActionBar(message) }
                MessageType.CHAT -> broadcast.broadcast(player) { it.sendMessage(message) }
                else -> return@execute // to get rid of IDE warnings
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
         * @see Title
         */
        TITLE;

        companion object {

            fun find(string: String): MessageType = when (string.uppercase()) {
                "ACTION", "ACTION_BAR" -> ACTION
                "CHAT" -> CHAT
                "TITLE" -> TITLE
                else -> CHAT
            }

        }

    }

}