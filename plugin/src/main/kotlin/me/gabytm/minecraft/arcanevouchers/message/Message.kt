package me.gabytm.minecraft.arcanevouchers.message

import me.gabytm.minecraft.arcanevouchers.Constant
import net.kyori.adventure.audience.Audience

abstract class Message(protected val string: String) {

    abstract fun send(player: Audience, args: Map<String, String> = emptyMap())

    companion object {

        val NONE = object : Message("") {
            override fun send(player: Audience, args: Map<String, String>) { }
        }

        fun create(string: String): Message {
            if (string.isEmpty()) {
                return MessageType.NONE.create("")
            }

            val parts = string.split(Constant.Separator.COLON, 2)

            if (parts.size == 1) {
                return MessageType.CHAT.create(parts[0])
            }

            return when (parts[0].lowercase()) {
                "actionbar" -> MessageType.ACTION_BAR
                "chat" -> MessageType.CHAT
                "title" -> MessageType.TITLE
                else -> MessageType.CHAT
            }.create(parts[1])
        }

    }

}