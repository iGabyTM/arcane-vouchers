package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import com.google.common.base.Enums
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import de.tr7zw.nbtapi.NbtApiException
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.giveItems
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private fun String.fixSpace(): String {
        var string = SPACE.replace(this, " ")
        string = ESCAPED_UNDERSCORE.replace(string, "_")
        return string
    }

    private fun parseBaseItem(materialString: String, amountString: String): ItemStack? {
        val parts = materialString.split(Constant.Separator.COLON, 2)

        val material: Material
        var damage: Short = 0

        if (parts.size == 1) {
            material = Material.getMaterial(materialString) ?: kotlin.run {
                warning("Unknown material $materialString")
                return null
            }
        } else {
            material = Material.getMaterial(parts[0].uppercase()) ?: kotlin.run {
                warning("Unknown material ${parts[0]} ($materialString)")
                return null
            }
            damage = parts[1].toShortOrNull() ?: return null
        }

        if (material == Material.AIR) {
            return null
        }

        val amount = amountString.toIntOrNull() ?: return null
        return ItemStack(material, amount, damage)
    }

    private fun createItem(string: String): ItemStack? {
        val tokenizer = StringTokenizer(string, " ")

        if (tokenizer.countTokens() < 2) {
            return null
        }

        val builder = ItemBuilder.from(parseBaseItem(tokenizer.nextToken().uppercase(), tokenizer.nextToken()) ?: return null)

        while (tokenizer.hasMoreTokens()) {
            val parts = tokenizer.nextToken().split(Constant.Separator.COLON, 2)
            val key = parts[0]
            val value = parts[1]

            when (key.lowercase()) {
                "name" -> builder.name(value.fixSpace().mini())

                "lore" -> builder.lore(
                    NEW_LINE.split(value)
                        .map { it.fixSpace() }
                        .map { ESCAPED_VERTICAL_LINE.replace(it, "|") }
                        .map { it.mini() }
                )

                "flags" -> {
                    val flags = value.split(Constant.Separator.COMMA)
                        .mapNotNull { Enums.getIfPresent(ItemFlag::class.java, it.uppercase()).orNull() }
                        .toTypedArray()
                    builder.flags(*flags)
                }

                "unbreakable" -> builder.unbreakable()

                "model" -> value.toIntOrNull()?.let { builder.model(it) }

                "nbt" -> {
                    val json = string.substring(string.indexOf("nbt:") + 4)

                    try {
                        return NBTItem(builder.build()).apply { mergeCompound(NBTContainer(json)) }.item
                    } catch (e: NbtApiException) {
                        exception("Could not parse nbt '$json'", e)
                    }
                }

                else -> {
                    Enchantment.getByName(key)?.let {
                        val level = value.toIntOrNull() ?: return@let
                        builder.enchant(it, level)
                    }
                }
            }
        }

        return builder.build()
    }

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val item = createItem(meta.getParsedData(player, context)) ?: return@execute
            player.giveItems(item)
        }
    }

    companion object {

        private val ESCAPED_UNDERSCORE = Regex("\\\\_")
        private val ESCAPED_VERTICAL_LINE = Regex("\\\\\\|")

        private val NEW_LINE = Regex("(?<!\\\\)\\|")
        private val SPACE = Regex("(?<!\\\\)_")

    }

}