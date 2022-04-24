package me.gabytm.minecraft.arcanevouchers.actions.implementations.vault

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player

class AddMoneyAction(
    meta: ActionMeta<Player>,
    handler: PermissionHandler,
    private val economy: Economy
) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val parsedData = meta.getParsedData(player, context)
            val amount = parsedData.toDoubleOrNull() ?: kotlin.run {
                warning("Could not parse double from '$parsedData' ($ID, '${meta.rawData}')")
                return@execute
            }
            economy.depositPlayer(player, amount)
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "addmoney"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(
                Component.text("Add money to player's balance")
                    .append(Component.newline())
                    .append(Component.text("An economy plugin is required to use this action", NamedTextColor.GREEN))
            )
            // Required arguments
            .argument("amount") {
                type(UsageBuilder.DOUBLE)
                    .description("the amount of money to give")
                    .required()
            }
            .build()

    }

}