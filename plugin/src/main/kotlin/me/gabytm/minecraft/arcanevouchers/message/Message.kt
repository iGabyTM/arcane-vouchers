package me.gabytm.minecraft.arcanevouchers.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.mini
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.regex.Pattern

abstract class Message(protected val string: String) {

    private fun getPlaceholder(stringPlaceholder: String): Pattern {
        val placeholder = PLACEHOLDERS[stringPlaceholder]

        if (placeholder != null) {
            return placeholder
        }

        val newPlaceholder = Pattern.quote(stringPlaceholder).toPattern(Pattern.CASE_INSENSITIVE)
        PLACEHOLDERS[stringPlaceholder] = newPlaceholder
        return newPlaceholder
    }

    protected fun format(
        message: Component,
        strings: Map<String, String>,
        components: Map<String, Component>
    ): Component {
        if (strings.isEmpty() && components.isEmpty()) {
            return message
        }

        val placeholdersAndValues = components.toMutableMap()
        strings.forEach { (placeholder, value) -> placeholdersAndValues[placeholder] = value.mini() }

        var replaced = message

        for ((placeholder, value) in placeholdersAndValues) {
            replaced = replaced.replaceText { it.match(getPlaceholder(placeholder)).replacement(value) }
        }

        return replaced
    }

    abstract fun send(player: Audience, strings: Map<String, String> = emptyMap(), components: Map<String, Component> = emptyMap())

    companion object {

        private val PLACEHOLDERS = mutableMapOf<String, Pattern>()

        val NO_OP = object : Message("") {
            // no-op
            override fun send(player: Audience, strings: Map<String, String>, components: Map<String, Component>) {}
        }

        fun create(string: String): Message {
            if (string.isEmpty()) {
                return MessageType.NONE.create("")
            }

            val parts = string.split(Constant.Separator.SEMICOLON, 2)

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