package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import org.bukkit.entity.Player

class WorldGuard7Compat : WorldGuardCompat {

    private fun isInside(player: Player, regions: List<String>): Boolean {
        return WorldGuard.getInstance().platform.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(player.location))
            .any { regions.contains(it.id) }
    }

    override fun isWhitelisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) true else isInside(player, regions)
    }

    override fun isBlacklisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) false else isInside(player, regions)
    }

}