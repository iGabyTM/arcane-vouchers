package me.gabytm.minecraft.arcanevouchers.message.implementations

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.message.Message
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title

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

}