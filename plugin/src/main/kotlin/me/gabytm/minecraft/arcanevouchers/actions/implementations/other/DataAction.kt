package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import me.gabytm.util.actions.actions.implementations.DataAction
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class DataAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val base = DataAction(meta)

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            base.run(player, context)
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "data"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Store a value that can be reused on the next actions"))
            // Required arguments
            .argument("key") {
                type(UsageBuilder.STRING)
                    .description("the key of the value, must not contain spaces")
                    .required()
            }
            .argument("value") {
                type(UsageBuilder.STRING)
                    .description("the value")
                    .required()
            }
            .build()

    }

}