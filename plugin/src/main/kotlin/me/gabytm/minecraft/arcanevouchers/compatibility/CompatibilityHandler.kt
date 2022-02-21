package me.gabytm.minecraft.arcanevouchers.compatibility

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.compatibility.worldguard.WorldGuard6Compatibility
import me.gabytm.minecraft.arcanevouchers.compatibility.worldguard.WorldGuard7Compatibility
import me.gabytm.minecraft.arcanevouchers.compatibility.worldguard.WorldGuardCompatibility
import org.bukkit.Bukkit

class CompatibilityHandler(plugin: ArcaneVouchers) {

    /**
     * **ALWAYS** check if [hasWorldGuardSupport] is true before accessing [worldGuardCompatibility]
     */
    var hasWorldGuardSupport: Boolean = false; private set
    lateinit var worldGuardCompatibility: WorldGuardCompatibility private set

    init {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            val version = Bukkit.getPluginManager().getPlugin("WorldGuard")!!.description.version

            when {
                version.startsWith("6.") -> WorldGuard6Compatibility()
                version.startsWith("7.") -> WorldGuard7Compatibility()
                else -> {
                    plugin.logger.warning("Found an unsupported WorldGuard version, $version")
                    null
                }
            }?.let {
                plugin.logger.info("Added support for WorldGuard v${version[0]} ($version)")
                this.hasWorldGuardSupport = true
                this.worldGuardCompatibility = it
            }
        }
    }

}