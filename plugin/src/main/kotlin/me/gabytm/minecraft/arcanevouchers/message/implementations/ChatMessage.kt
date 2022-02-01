package me.gabytm.minecraft.arcanevouchers.message.implementations

import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.message.Message
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

class ChatMessage(string: String) : Message(string) {

    private val message = string.mini()

    override fun send(player: Audience, strings: Map<String, String>, components: Map<String, Component>) {
        player.sendMessage(format(message, strings, components))
    }

}