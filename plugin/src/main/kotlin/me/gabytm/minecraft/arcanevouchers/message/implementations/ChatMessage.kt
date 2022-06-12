package me.gabytm.minecraft.arcanevouchers.message.implementations

import com.google.gson.*
import me.gabytm.minecraft.arcanevouchers.commands.commands.DebugCommand
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.message.Message
import me.gabytm.minecraft.arcanevouchers.message.MessageType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import java.lang.reflect.Type

class ChatMessage(string: String) : Message(string) {

    private val message = string.mini()

    override fun send(player: Audience, strings: Map<String, String>, components: Map<String, Component>) {
        player.sendMessage(format(message, strings, components))
    }

    internal class Serializer private constructor(): JsonSerializer<ChatMessage> {

        override fun serialize(src: ChatMessage?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            if (src == null) {
                return JsonNull.INSTANCE
            }

            val obj = JsonObject()
            obj.addProperty("type", MessageType.CHAT.name)
            obj.add("message", DebugCommand.GSON.toJsonTree(src.message, TextComponent::class.java))
            return obj
        }

        companion object {
            val INSTANCE = Serializer()
        }

    }

}