package me.gabytm.minecraft.arcanevouchers.compatibility.worldguard

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Location

class WorldGuard6Compatibility : WorldGuardCompatibility {

    /**
     * Gets the id of all regions from a certain [Location]
     * @return set of region ids
     */
    override fun getRegions(location: Location): Set<String> {
        return WorldGuardPlugin.inst().regionContainer.createQuery().getApplicableRegions(location)
            .map { it.id }
            .toSet()
    }

}