package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.processArguments
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class VoucherAction(
    meta: ActionMeta<Player>,
    handler: PermissionHandler,
    private val voucherManager: VoucherManager
) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            // Format: <voucher> (amount: 1) (args...)
            val parts = meta.getParsedData(player, context).split(Constant.Separator.SPACE, 3)

            val voucher = voucherManager.getVoucher(parts[0]) ?: kotlin.run {
                warning("Unknown voucher ${parts[0]} (voucher, ${meta.rawData}")
                return@execute
            }
            val amount = if (parts.size == 1) 1 else parts[1].toIntOrNull() ?: kotlin.run {
                warning("Invalid amount ${parts[1]} (voucher, ${meta.rawData})")
                return@execute
            }
            val args = if (parts.size < 3) emptyArray() else parts[2].processArguments()

            voucherManager.giveVoucher(player, voucher, amount, args)
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "voucher"

        private val USAGE: Component = UsageBuilder(ID)
            .hover(Component.text("Give vouchers to the player"))
            // Required arguments
            .argument("name") {
                type(UsageBuilder.STRING)
                    .description("the name of a voucher")
                    .required()
            }
            // Optional arguments
            .argument("amount") {
                type(UsageBuilder.INTEGER)
                    .description("the amount of vouchers to give")
                    .default(1)
            }
            .argument("args") {
                type(UsageBuilder.STRING)
                    .description("similar to the '/av give' command")
                    .default("none")
            }
            .build()

    }

}