package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.giveItems
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
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

    companion object {

        private val usage = UsageBuilder("item")
            .hover(
                text("Give to the player a custom item")
                    .append(newline())
                    .append(
                        text("A format similar to EssentialsX's is used", NamedTextColor.GRAY, TextDecoration.ITALIC))
            )
            // Arguments
            .required(
                true,
                "material(:damage)",
                UsageBuilder.STRING,
                "name of a Material. To specify its damage, add :damage after (INK_SACK:4 for lapiz lazuli on < 1.13)"
            )
            .required(true, "amount", UsageBuilder.INTEGER, "the amount of the item")
            .optional(true, "name", UsageBuilder.STRING, "the name of the item, use _ for space")
            .optional(true, "lore", UsageBuilder.STRING, "the lore of the item, use _ for space and | for a new line")
            .optional(true, "flags", UsageBuilder.LIST, "a comma separated list of ItemFlag names")
            .optional(
                true,
                "unbreakable",
                UsageBuilder.BOOLEAN,
                "make the item unbreakable, this argument doesn't require a value"
            )
            .optional(
                true,
                "model",
                UsageBuilder.INTEGER,
                "the custom model data of this item",
                condition = ServerVersion.HAS_CUSTOM_MODEL_DATA
            )
            .optional(
                true,
                "nbt",
                UsageBuilder.STRING,
                "a JSON string representing item's NBT, some escaping should be done, mostly for quotes. This argument must be the last one since it takes everything that's after nbt: as value"
            )
            .build()

        @Suppress("unused")
        @JvmStatic
        private fun usage(): Component = usage

    }

}