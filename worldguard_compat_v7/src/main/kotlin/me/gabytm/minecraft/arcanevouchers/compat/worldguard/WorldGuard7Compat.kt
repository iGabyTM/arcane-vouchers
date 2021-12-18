package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Location

class WorldGuard7Compat : WorldGuardCompat {

    override fun getRegions(location: Location): Set<String> {
        return WorldGuard.getInstance().platform.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(location))
            .map { it.id }
            .toSet()
    }

}