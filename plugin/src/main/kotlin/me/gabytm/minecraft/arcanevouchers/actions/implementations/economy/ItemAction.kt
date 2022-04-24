package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.giveItems
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

class ItemAction(meta: ActionMeta<Player>, handler: PermissionHandler, private val itemCreator: ItemCreator) :
    ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            player.giveItems(itemCreator.create(meta.getParsedData(player, context)) ?: return@execute)
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID = "item"

        private val USAGE = UsageBuilder(ID)
            .hover(
                text("Give to the player a custom item")
                    .append(newline())
                    .append(
                        text("A format similar to EssentialsX's is used", NamedTextColor.GRAY, TextDecoration.ITALIC)
                    )
            )
            // Required arguments
            .argument("material(:damage)") {
                type(UsageBuilder.STRING)
                    .description("name of a Material. To specify its damage, add :damage after (INK_SACK:4 for Lapiz Lazuli on < 1.13)")
                    .required()
            }
            .argument("amount") {
                type(UsageBuilder.INTEGER)
                    .description("the amount of the item")
                    .required()
            }
            // Optional arguments
            .argument("name") {
                type(UsageBuilder.STRING)
                    .description("the name of the item, use _ for space")
            }
            .argument("lore") {
                type(UsageBuilder.STRING)
                    .description("the lore of the item, use _ for space and | for a new line")
            }
            .argument("flags") {
                type(UsageBuilder.LIST)
                    .description("a comma separated list of ItemFlag names")
            }
            .argument("unbreakable") {
                type(UsageBuilder.BOOLEAN)
                    .description("make the item unbreakable, this argument doesn't require a value")
            }
            .argument(
                "mode",
                ServerVersion.HAS_CUSTOM_MODEL_DATA
            ) {
                type(UsageBuilder.INTEGER)
                    .description("the custom model data of this item")
            }
            .argument("nbt") {
                type(UsageBuilder.STRING)
                    .description("a JSON string representing item's NBT, some escaping should be done, mostly for quotes. This argument must be the last one since it takes everything that's after nbt: as value")
            }
            .build()

    }

}