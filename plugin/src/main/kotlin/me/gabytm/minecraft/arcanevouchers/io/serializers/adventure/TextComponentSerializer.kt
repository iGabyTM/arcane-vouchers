package me.gabytm.minecraft.arcanevouchers.io.serializers.adventure

import com.google.gson.*
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import java.lang.reflect.Type

class TextComponentSerializer : JsonSerializer<TextComponent> {

    override fun serialize(src: TextComponent?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(MiniMessage.miniMessage().serialize(src))
    }

}