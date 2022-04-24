package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class ChatAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            player.chat(meta.getParsedData(player, context))
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "chat"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Make the player send a message in chat"))
            // Required arguments
            .argument("message") {
                type(UsageBuilder.STRING)
                    .description("the message that will be sent")
                    .required()
            }
            .build()

    }

}