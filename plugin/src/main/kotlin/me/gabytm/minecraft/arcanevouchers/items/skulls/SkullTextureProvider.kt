package me.gabytm.minecraft.arcanevouchers.items.skulls

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.builder.item.SkullBuilder
import me.gabytm.minecraft.arcanevouchers.Constant

interface SkullTextureProvider {

    fun apply(input: String): SkullBuilder

    enum class Type(private val provider: SkullTextureProvider) {

        BASE_64(Base64TextureProvider()),
        HEAD_DATABASE(HdbTextureProvider()),
        PLAYER_NAME(PlayerNameTextureProvider()),

        NONE(SkullTextureProvider.NONE);

        fun apply(input: String): SkullBuilder = provider.apply(input)

    }

    companion object {

        val NONE = object : SkullTextureProvider {
            override fun apply(input: String): SkullBuilder = ItemBuilder.skull()
        }

        fun applyTexture(input: String): SkullBuilder {
            val parts = input.split(Constant.Separator.COLON, 2)

            if (parts.size == 1) {
                return Type.NONE.apply(parts[0])
            }

            return when (parts[0].lowercase()) {
                "base64" -> Type.BASE_64
                "hdb" -> Type.HEAD_DATABASE
                "player" -> Type.PLAYER_NAME
                else -> Type.NONE
            }.apply(parts[1])
        }

    }

}