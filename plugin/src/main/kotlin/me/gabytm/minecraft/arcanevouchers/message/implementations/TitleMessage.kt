package me.gabytm.minecraft.arcanevouchers.message.implementations

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.functions.replace
import me.gabytm.minecraft.arcanevouchers.message.Message
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.title.Title

class TitleMessage(string: String) : Message(string) {

    private val title: String
    private val subtitle: String

    init {
        val parts = string.split(Constant.Separator.NEW_LINE, 2)

        title = parts[0]
        subtitle = if (parts.size == 2) parts[1] else ""
    }

    override fun send(player: Audience, args: Map<String, String>) {
        player.showTitle(Title.title(title.replace(args).mini(), subtitle.replace(args).mini()))
    }

}