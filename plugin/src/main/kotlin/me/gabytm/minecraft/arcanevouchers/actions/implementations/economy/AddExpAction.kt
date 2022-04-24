package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class AddExpAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            // Format: <amount>(L)
            val parsed = meta.getParsedData(player, context)

            if (parsed.endsWith('L')) {
                // Remove the L at the end
                val levels = parsed.substring(0, parsed.length - 2).toIntOrNull() ?: kotlin.run {
                    warning("Could not parse integer from '$parsed' (addexp, ${meta.rawData})")
                    return@execute
                }
                player.giveExpLevels(levels)
            } else {
                val exp = parsed.toIntOrNull() ?: kotlin.run {
                    warning("Could not parse integer from '$parsed' (addexp, ${meta.rawData})")
                    return@execute
                }
                player.giveExp(exp)
            }
        }
    }

    @Suppress("unused", "SpellCheckingInspection")
    companion object {

        private const val ID: String = "addexp"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(
                Component.text("Give to the player an amount of ")
                    .append(Component.text("EXP", NamedTextColor.GREEN))
            )
            // Required arguments
            .argument("amount(L)") {
                type(UsageBuilder.INTEGER)
                    .description("amount of exp to add. By appending an 'L' after, levels of exp will be given instead of points")
                    .required()
            }
            .build()

    }

}