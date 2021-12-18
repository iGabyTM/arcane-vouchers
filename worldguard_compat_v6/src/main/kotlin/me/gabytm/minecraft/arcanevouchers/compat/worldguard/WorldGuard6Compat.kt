package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.entity.Player

class WorldGuard6Compat : WorldGuardCompat {

    private fun isInside(player: Player, regions: List<String>): Boolean {
        return WorldGuardPlugin.inst().regionContainer.createQuery().getApplicableRegions(player.location)
            .any { regions.contains(it.id) }
    }

    override fun isWhitelisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) true else isInside(player, regions)
    }

    override fun isBlacklisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) false else isInside(player, regions)
    }

}