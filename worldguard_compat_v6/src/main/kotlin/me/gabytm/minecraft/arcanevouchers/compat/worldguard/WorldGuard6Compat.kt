package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Location

class WorldGuard6Compat : WorldGuardCompat {

    override fun getRegions(location: Location): Set<String> {
        return WorldGuardPlugin.inst().regionContainer.createQuery().getApplicableRegions(location)
            .map { it.id }
            .toSet()
    }

}