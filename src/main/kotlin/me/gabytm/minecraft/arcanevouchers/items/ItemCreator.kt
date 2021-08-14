package me.gabytm.minecraft.arcanevouchers.items

import com.google.common.base.Enums
import dev.triumphteam.gui.builder.item.BaseItemBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.isPlayerHead
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.items.skulls.SkullTextureProvider
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemCreator(private val config: ConfigurationSection, private val defaultMaterial: Material? = null) {

    // TODO: 14-Aug-21 add the other stuff 
    private fun setGeneralMeta(itemBuilder: BaseItemBuilder<*>): BaseItemBuilder<*> {
        val flags = config.getStringList("flags")
            .mapNotNull { Enums.getIfPresent(ItemFlag::class.java, it.uppercase()).orNull() }
            .toTypedArray()

        val enchants = config.getStringList("enchantments")
            .map { it.split(Constant.SEPARATOR, 2) }
            .mapNotNull {
                val enchantment = Enchantment.getByName(it[0]) ?: return@mapNotNull null
                val level = it[1].toIntOrNull() ?: return@mapNotNull null
                enchantment to level
            }.toMap()

        val customModelData = config.getInt("customModelData")

        return itemBuilder
            .name(config.getString("name")?.mini() ?: Component.empty())
            .lore(config.getStringList("lore").map { it.mini() })
            .flags(*flags)
            .apply {
                enchants.forEach { (enchantment, level) -> enchant(enchantment, level, true) }

                if (customModelData > 0) {
                    model(customModelData)
                }
            }
    }

    // TODO: 14-Aug-21 add support for banners 
    fun create(): ItemStack? {
        val materialString = config.getString("material") ?: ""
        val material = if (materialString.isEmpty()) {
            defaultMaterial
        } else {
            Material.matchMaterial(materialString, false)
        } ?: return null

        val damage = config.getInt("damage").toShort()

        if (material.isPlayerHead(damage)) {
            return setGeneralMeta(SkullTextureProvider.applyTexture(config.getString("") ?: "")).build()
        }

        return setGeneralMeta(ItemBuilder.from(ItemStack(material, 1, damage))).build()
    }

}