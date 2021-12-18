package me.gabytm.minecraft.arcanevouchers.items

import com.google.common.base.Enums
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import dev.triumphteam.gui.builder.item.BaseItemBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.isPlayerHead
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.items.skulls.SkullTextureProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemCreator(private val plugin: ArcaneVouchers) {

    private val nbtHandler = NBTHandler(plugin)

    /**
     * Turn a list with 2 elements into a Pair<[Enchantment], [Int]>
     * @return pair or null if the enchantment or level are null
     */
    private fun List<String>.toEnchantmentPair(): Pair<Enchantment, Int>? {
        val enchantment = Enchantment.getByName(first()) ?: return null
        val level = get(1).toIntOrNull() ?: return null
        return enchantment to level
    }

    /**
     * Set the general meta to the item and build it
     * @param isVoucher whether the item is a voucher
     * @param config the section from where values are read
     * @return an [ItemStack] created according to the specified values
     */
    // TODO: 9/17/2021 add support for color
    private fun BaseItemBuilder<*>.setGeneralMeta(isVoucher: Boolean, config: ConfigurationSection): ItemStack {
        val flags = config.getStringList("flags")
            .mapNotNull { Enums.getIfPresent(ItemFlag::class.java, it.uppercase()).orNull() }
            .toTypedArray()

        // Enchantments are saved in a list as 'Enchantment;level'
        val enchants = config.getStringList("enchantments")
            .map { it.split(Constant.SEPARATOR, 2) }
            .mapNotNull { it.toEnchantmentPair() }.toMap()

        val customModelData = config.getInt("customModelData")

        val item = this
            .name(config.getString("name")?.mini() ?: Component.empty())
            .lore(config.getStringList("lore").map { it.mini() })
            .flags(*flags)
            .unbreakable(config.getBoolean("unbreakable"))
            .apply {
                enchants.forEach { (enchantment, level) -> enchant(enchantment, level, true) }

                if (customModelData > 0) {
                    model(customModelData)
                }

                if (enchants.isEmpty()) {
                    glow(config.getBoolean("glow"))
                }
            }.build()

        if (isVoucher) {
            // Get the voucher ID
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
            Material.matchMaterial(materialString, false)
        }

        // If the material specified is null return a fallback item
        if (material == null) {
            return ItemBuilder.from(Material.PAPER)
                .name(Component.text("Unknown material $materialString", NamedTextColor.RED))
                .build()
        }

        val damage = config.getInt("damage").toShort()

        // Decide what ItemBuilder implementation should be used
        val builder = if (material.isPlayerHead(damage)) {
            SkullTextureProvider.applyTexture(config.getString("skull") ?: "")
        } else {
            ItemBuilder.from(ItemStack(material, 1, damage))
        }

        // Set the general meta to the item and build it
        return builder.setGeneralMeta(isVoucher, config)
    }

}