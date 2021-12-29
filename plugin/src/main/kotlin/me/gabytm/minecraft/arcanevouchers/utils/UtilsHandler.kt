package me.gabytm.minecraft.arcanevouchers.utils

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.error
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class UtilsHandler(plugin: ArcaneVouchers) {

    init {
        val file = File(plugin.dataFolder, "UTILS.yml")

        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                error("Could not create ${file.path}", e)
            }
        }

        val yaml = YamlConfiguration.loadConfiguration(file)
        yaml.options().header("\nHelpful lists of data generated for Minecraft ${Bukkit.getBukkitVersion()}\n ")

        yaml["bossBar.colors"] = BossBar.Color.NAMES.keys().toList()
        yaml["bossBar.flags"] = BossBar.Flag.NAMES.keys().toList()
        yaml["bossBar.overlays"] = BossBar.Overlay.NAMES.keys().toList()
        yaml["bossBar.progress.min"] = BossBar.MIN_PROGRESS
        yaml["bossBar.progress.max"] = BossBar.MAX_PROGRESS

        yaml["materials"] = Material.values().map { it.name }

        try {
            yaml.save(file)
        } catch (e: IOException) {
            error("Could not save ${file.path}", e)
        }
    }

}