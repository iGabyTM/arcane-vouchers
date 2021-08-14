package me.gabytm.minecraft.arcanevouchers.items.skulls

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.builder.item.SkullBuilder
import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.Bukkit

class HdbTextureProvider : SkullTextureProvider {

    private lateinit var api: HeadDatabaseAPI
    private val enabled = Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")

    init {
        if (enabled) {
            this.api = HeadDatabaseAPI()
        }
    }

    override fun apply(input: String): SkullBuilder {
        if (enabled) {
            val skull = api.getItemHead(input) ?: return ItemBuilder.skull()
            return ItemBuilder.skull(skull)
        }

        return ItemBuilder.skull()
    }

}