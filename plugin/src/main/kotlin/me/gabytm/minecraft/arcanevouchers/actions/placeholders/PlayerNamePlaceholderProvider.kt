package me.gabytm.minecraft.arcanevouchers.actions.placeholders

import me.gabytm.util.actions.placeholders.PlaceholderProvider
import org.bukkit.entity.Player

class PlayerNamePlaceholderProvider : PlaceholderProvider<Player> {

    override fun getType(): Class<Player> {
        return Player::class.java
    }

    override fun replace(player: Player, input: String): String {
        return input.replace("%player_name%", player.name)
    }

}