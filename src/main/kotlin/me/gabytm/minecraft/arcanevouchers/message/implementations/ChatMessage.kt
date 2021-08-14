package me.gabytm.minecraft.arcanevouchers.message.implementations

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.replace
import me.gabytm.minecraft.arcanevouchers.message.Message
import net.kyori.adventure.audience.Audience

class ChatMessage(string: String) : Message(string) {

    override fun send(player: Audience, args: Map<String, String>) {
        player.sendMessage(Constant.MINI.parse(string.replace(args)))
    }

}