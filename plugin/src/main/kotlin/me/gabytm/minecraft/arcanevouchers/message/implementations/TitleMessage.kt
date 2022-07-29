package me.gabytm.minecraft.arcanevouchers.message.implementations

import com.google.gson.*
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.commands.DebugCommand
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.message.Message
import me.gabytm.minecraft.arcanevouchers.message.MessageType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.title.Title
import java.lang.reflect.Type

class TitleMessage(string: String) : Message(string) {

    private val title: Component
    private val subtitle: Component

    init {
        val parts = string.split(Constant.Separator.NEW_LINE, 2)

        title = parts[0].mini()
        subtitle = if (parts.size == 2) parts[1].mini() else Component.empty()
    }

    override fun send(player: Audience, strings: Map<String, String>, components: Map<String, Component>) {
        player.showTitle(
            Title.title(
                format(title, strings, components),
                format(subtitle, strings, components)
            )
        )
    }

    internal class Serializer private constructor() : JsonSerializer<TitleMessage> {

        override fun serialize(src: TitleMessage?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            if (src == null) {
                return JsonNull.INSTANCE
            }

            val obj = JsonObject()
            obj.addProperty("type", MessageType.TITLE.name)
            obj.add("title", DebugCommand.GSON.toJsonTree(src.title, TextComponent::class.java))
            obj.add("subtitle", DebugCommand.GSON.toJsonTree(src.subtitle, TextComponent::class.java))
            return obj
        }

        companion object {
            val INSTANCE = Serializer()
        }

    }

}