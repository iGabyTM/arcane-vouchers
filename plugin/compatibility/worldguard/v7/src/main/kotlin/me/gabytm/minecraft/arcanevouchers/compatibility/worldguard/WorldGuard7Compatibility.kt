package me.gabytm.minecraft.arcanevouchers.compatibility.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Location

class WorldGuard7Compatibility : WorldGuardCompatibility {

    /**
     * Gets the id of all regions from a certain [Location]
     * @return set of region ids
     */
    override fun getRegions(location: Location): Set<String> {
        return WorldGuard.getInstance().platform.regionContainer.createQuery().getApplicableRegions(BukkitAdapter.adapt(location))
            .map { it.id }
            .toSet()
    }

}