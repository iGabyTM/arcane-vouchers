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

class ActionBarMessage(string: String) : Message(string) {

    private val message = string.mini()

    override fun send(player: Audience, strings: Map<String, String>, components: Map<String, Component>) {
        player.sendActionBar(format(message, strings, components))
    }

    internal class Serializer private constructor(): JsonSerializer<ActionBarMessage> {

        override fun serialize(src: ActionBarMessage?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            if (src == null) {
                return JsonNull.INSTANCE
            }

            val obj = JsonObject()
            obj.addProperty("type", MessageType.ACTION_BAR.name)
            obj.add("message", DebugCommand.GSON.toJsonTree(src.message, TextComponent::class.java))
            return obj
        }

        companion object {
            val INSTANCE = Serializer()
        }

    }

}