package me.gabytm.minecraft.arcanevouchers.utils

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.exception
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.PatternType
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UtilsHandler(plugin: ArcaneVouchers) {

    init {
        val file = File(plugin.dataFolder, "UTILS.yml")

        if (file.exists()) {
            file.delete()
        }

        try {
            file.createNewFile()
        } catch (e: IOException) {
            exception("Could not create ${file.path}", e)
        }

        val time = SimpleDateFormat("dd MMM yyyy, HH:mm z").format(Date())
        val yaml = YamlConfiguration.loadConfiguration(file)
        yaml.options().header("\nHelpful lists of data generated for Minecraft ${Bukkit.getBukkitVersion()} at $time\n ")

        yaml["bossBar.colors"] = BossBar.Color.NAMES.keys().toList()
        yaml["bossBar.flags"] = BossBar.Flag.NAMES.keys().toList()
        yaml["bossBar.overlays"] = BossBar.Overlay.NAMES.keys().toList()
        yaml["bossBar.progress.min"] = BossBar.MIN_PROGRESS
        yaml["bossBar.progress.max"] = BossBar.MAX_PROGRESS

        yaml["dyeColors"] = DyeColor.values().map { it.name }

        yaml["enchantments"] = if (ServerVersion.HAS_KEYS) {
            Enchantment.values().map { it.key.toString() }
        } else {
            Enchantment.values().map { it.name }
        }

        yaml["itemFlags"] = ItemFlag.values().map { it.name }

        yaml["materials"] = Material.values().map { it.name }.filter { !it.startsWith("LEGACY_") }

        yaml["patternTypes"] = PatternType.values().map { it.name }

        yaml["sound.sources"] = Sound.Source.NAMES.keys().toList()

        if (ServerVersion.HAS_KEYS) {
            yaml["sound.sounds"] = org.bukkit.Sound.values().map { it.key.toString() }
        }

        try {
            yaml.save(file)
        } catch (e: IOException) {
            exception("Could not save ${file.path}", e)
        }
    }

}