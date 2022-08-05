package me.gabytm.minecraft.arcanevouchers.items

import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import de.tr7zw.nbtapi.NbtApiException
import dev.triumphteam.gui.builder.item.BaseItemBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.*
import me.gabytm.minecraft.arcanevouchers.items.skulls.SkullTextureProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemCreator(plugin: ArcaneVouchers) {

    val nbtHandler = NBTHandler(plugin)

    /**
     * Turn a list with 2 elements into a Pair<[Enchantment], [Int]>
     * @return pair or null if the enchantment or level are null
     */
    @Suppress("DEPRECATION")
    private fun List<String>.toEnchantmentPair(): Pair<Enchantment, Int>? {
        val enchantment = if (ServerVersion.HAS_KEYS) {
            val parts = first().split(Constant.Separator.COLON, 2)

            if (parts.size == 2) {
                Enchantment.getByKey(NamespacedKey(parts[0], parts[1]))
            } else {
                // The server has NamespacedKeys but this isn't one
                Enchantment.getByName(first())
            }
        } else {
            Enchantment.getByName(first())
        } ?: kotlin.run {
            warning("Unknown enchantment ${first()}")
            return null
        }
        val level = get(1).toIntOrNull() ?: return null
        return enchantment to level
    }

    /**
     * Set the general meta to the item and build it
     * @param isVoucher whether the item is a voucher
     * @param config the section from where values are read
     * @return an [ItemStack] created according to the specified values
     */
    private fun BaseItemBuilder<*>.setGeneralMeta(isVoucher: Boolean, config: ConfigurationSection): ItemStack {
        val flags = config.getStringList("flags")
            .mapNotNull { it.toEnumValue<ItemFlag>() }
            .toTypedArray()

        // Enchantments are saved in a list as 'Enchantment;level'
        val enchants = config.getStringList("enchantments")
            .map { it.split(Constant.Separator.SEMICOLON, 2) }
            .mapNotNull { it.toEnchantmentPair() }.toMap()

        val customModelData = config.getInt("customModelData")

        val item = this
            .name(config.getString("name")?.mini(true) ?: Component.empty())
            .lore(config.getStringList("lore").map { it.mini(true) })
            .flags(*flags)
            .enchant(enchants, true)
            .unbreakable(config.getBoolean("unbreakable"))
            .apply {
                if (customModelData > 0) {
                    model(customModelData)
                }

                if (enchants.isEmpty()) {
                    glow(config.getBoolean("glow"))
                }

                config.getString("color")?.toColor()?.let { color(it) }
            }.build()

        if (isVoucher) {
            // Get the JSON of this voucher
            val nbt = nbtHandler.getNbt(config.parent?.name ?: "") ?: return item
            return NBTItem(item).apply { mergeCompound(NBTContainer(nbt)) }.item
        }

        return item
    }

    /**
     * Load the NBT string from file
     * @see NBTHandler.load
     */
    fun loadNbt() {
        this.nbtHandler.load()
    }

    /**
     * Create an item from a [ConfigurationSection]
     * @param isVoucher whether the item is a voucher
     * @param config the section from where values are read
     * @param defaultMaterial the default material used for when the config section is null or the specified material is
     *                        invalid
     * @return an [ItemStack] created according to the specified values
     */
    // TODO: 14-Aug-21 add support for banners
    fun create(isVoucher: Boolean, config: ConfigurationSection?, defaultMaterial: Material? = null): ItemStack {
        // Return a default item if the config section is null
        if (config == null) {
            // If even the default material is null return an *invalid config section* item
            if (defaultMaterial == null) {
                return ItemBuilder.from(Material.PAPER)
                    .name(Component.text("Invalid config section", NamedTextColor.RED))
                    .build()
            }

            // Return an item with the default material
            return ItemStack(defaultMaterial)
        }

        val materialString = config.getString("material") ?: ""
        val material = if (materialString.isEmpty()) {
            defaultMaterial
        } else {
            Material.matchMaterial(materialString)
        }

        // If the material specified is null return a fallback item
        if (material == null) {
            warning("Unknown material $materialString")
            return ItemBuilder.from(Material.PAPER)
                .name(Component.text("Unknown material $materialString", NamedTextColor.RED))
                .build()
        }

        val damage = config.getInt("damage").toShort()

        // Decide what ItemBuilder implementation should be used
        val builder = if (material.isPlayerHead(damage)) {
            SkullTextureProvider.applyTexture(config.getString("texture") ?: "")
        } else {
            ItemBuilder.from(ItemStack(material, 1, damage))
        }

        // Set the general meta to the item and build it
        return builder.setGeneralMeta(isVoucher, config)
    }

    /* ----- DESERIALIZE AN ITEM FROM A STRING ----- */

    private fun String.fixSpace(): String {
        return ESCAPED_UNDERSCORE.replace(SPACE.replace(this, " "), "_")
    }

    private fun parseBaseItem(materialString: String, amountString: String): ItemStack? {
        val parts = materialString.split(Constant.Separator.COLON, 2)

        val material: Material
        var damage: Short = 0

        // The material doesn't contain ':damage'
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

    fun create(string: String): ItemStack? {
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
                        .mapNotNull { it.toEnumValue<ItemFlag>() }
                        .toTypedArray()
                    builder.flags(*flags)
                }

                "unbreakable" -> builder.unbreakable()

                "model" -> value.toIntOrNull()?.let { builder.model(it) }

                "nbt" -> {
                    // Take everything that's after 'nbt:'
                    val sNbt = string.substringAfter("$key:")

                    try {
                        return NBTItem(builder.build()).apply { mergeCompound(NBTContainer(sNbt)) }.item
                    } catch (e: NbtApiException) {
                        exception("Could not parse SNBT '$sNbt'", e)
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

    companion object {

        private val ESCAPED_UNDERSCORE = Regex("\\\\_")
        private val ESCAPED_VERTICAL_LINE = Regex("\\\\\\|")

        private val NEW_LINE = Regex("(?<!\\\\)\\|")
        private val SPACE = Regex("(?<!\\\\)_")

    }

}