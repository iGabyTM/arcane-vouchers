package me.gabytm.minecraft.arcanevouchers.items.skulls

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.builder.item.SkullBuilder

class Base64TextureProvider : SkullTextureProvider {

    override fun apply(input: String): SkullBuilder {
        return ItemBuilder.skull().texture(input)
    }

}