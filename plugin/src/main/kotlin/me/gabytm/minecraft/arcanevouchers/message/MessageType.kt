package me.gabytm.minecraft.arcanevouchers.message

import me.gabytm.minecraft.arcanevouchers.message.implementations.ActionBarMessage
import me.gabytm.minecraft.arcanevouchers.message.implementations.ChatMessage
import me.gabytm.minecraft.arcanevouchers.message.implementations.TitleMessage

enum class MessageType {

    ACTION_BAR {
        override fun create(string: String): Message = ActionBarMessage(string)
    },
    CHAT {
        override fun create(string: String): Message = ChatMessage(string)
    },
    TITLE {
        override fun create(string: String): Message = TitleMessage(string)
    },

    NONE {
        override fun create(string: String): Message = Message.NONE
    };

    abstract fun create(string: String): Message

}