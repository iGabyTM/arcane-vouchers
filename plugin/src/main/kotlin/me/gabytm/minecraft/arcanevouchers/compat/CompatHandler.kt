package me.gabytm.minecraft.arcanevouchers.compat

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.compat.worldguard.WorldGuard6Compat
import me.gabytm.minecraft.arcanevouchers.compat.worldguard.WorldGuard7Compat
import me.gabytm.minecraft.arcanevouchers.compat.worldguard.WorldGuardCompat
import org.bukkit.Bukkit

class CompatHandler(plugin: ArcaneVouchers) {

    /**
     * **ALWAYS** check if [hasWorldGuardSupport] is true before accessing [worldGuardCompat]
     */
    var hasWorldGuardSupport: Boolean = false; private set
    lateinit var worldGuardCompat: WorldGuardCompat private set

    init {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            val version = Bukkit.getPluginManager().getPlugin("WorldGuard")!!.description.version

            when {
                version.startsWith("6.") -> WorldGuard6Compat()
                version.startsWith("7.") -> WorldGuard7Compat()
                else -> {
                    plugin.logger.warning("Found an unsupported WorldGuard version, $version")
                    null
                }
            }?.let {
                plugin.logger.info("Added support for WorldGuard v${version[0]} ($version)")
                this.hasWorldGuardSupport = true
                this.worldGuardCompat = it
            }
        }
    }

}