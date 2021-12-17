package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import org.bukkit.entity.Player

class WorldGuard7Compat : WorldGuardCompat {

    override fun isWhitelisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) {
            true
        } else {
            WorldGuard.getInstance().platform.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(player.location))
                .any { regions.contains(it.id) }
        }
    }

    override fun isBlacklisted(player: Player, regions: List<String>): Boolean {
        return if (regions.isEmpty()) {
            false
        } else {
            WorldGuard.getInstance().platform.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(player.location))
                .any { regions.contains(it.id) }
        }
    }

}