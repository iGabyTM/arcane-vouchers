package me.gabytm.minecraft.arcanevouchers.items.skulls

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.builder.item.SkullBuilder
import org.bukkit.Bukkit

class PlayerNameTextureProvider : SkullTextureProvider {

    override fun apply(input: String): SkullBuilder {
        return ItemBuilder.skull().owner(Bukkit.getOfflinePlayer(input))
    }

}