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
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class UtilsHandler(plugin: ArcaneVouchers) {

    init {
        val file = File(plugin.dataFolder, ".UTILS.yml")

        if (file.exists()) {
            file.delete()
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

        yaml["effect.types"] = PotionEffectType.values().map { it.name }

        yaml["enchantments"] = if (ServerVersion.HAS_KEYS) {
            Enchantment.values().map { it.key.toString() }
        } else {
            Enchantment.values().map { it.name }
        }

        yaml["itemFlags"] = ItemFlag.values().map { it.name }

        yaml["materials"] = getMaterials()

        yaml["patternTypes"] = PatternType.values().map { it.name }

        yaml["sound.sounds"] = getSounds()
        yaml["sound.sources"] = Sound.Source.NAMES.keys().toList()

        try {
            yaml.save(file)
        } catch (e: IOException) {
            exception("Could not save ${file.path}", e)
        }
    }

    private fun getMaterials(): List<String> {
        val filter: (Material) -> Boolean = if (ServerVersion.IS_LEGACY) {
            { it != Material.AIR && isItem(it) }
        } else {
            { it != Material.AIR && it.isItem && !it.isLegacy }
        }

        return Material.values().filter(filter).map { it.name }
    }

    private fun isItem(material: Material): Boolean {
        return when (material.name) {
            "ACACIA_DOOR", "BED_BLOCK", "BEETROOT_BLOCK",
            "BIRCH_DOOR", "BREWING_STAND", "BURNING_FURNACE",
            "CAKE_BLOCK", "CARROT", "CAULDRON", "COCOA",
            "CROPS", "DARK_OAK_DOOR", "DAYLIGHT_DETECTOR_INVERTED",
            "DIODE_BLOCK_OFF", "DIODE_BLOCK_ON", "DOUBLE_STEP",
            "DOUBLE_STEP_SLAB2", "ENDER_PORTAL", "END_GATEWAY",
            "FIRE", "FLOWER_POT", "FROSTED_ICE",
            "GLOWING_REDSTONE_ORE", "IRON_DOOR_BLOCK", "JUNGLE_DOOR",
            "LAVA", "MELON_STEM", "NETHER_WARDS",
            "PISTON_EXTENSION", "PISTON_MOVING_PIECE", "PORTAL",
            "POTATO", "PUMPKIN_STEM", "PURPUR_DOUBLE_STAB",
            "REDSTONE_COMPARATOR_OFF", "REDSTONE_COMPARATOR_ON", "REDSTONE_LAMP_ON",
            "REDSTONE_TORCH_OFF", "REDSTONE_WIRE", "SIGN_POST",
            "SKULL", "SPRUCE_DOOR", "STANDING_BANNER",
            "STATIONARY_LAVA", "STATIONARY_WATER", "SUGAR_CANE_BLOCK",
            "TRIPWIRE", "WALL_BANNER", "WALL_SIGN",
            "WATER", "WOODEN_DOOR", "WOOD_DOUBLE_STEP" -> false
            else -> true
        }
    }

    private fun getSounds(): List<String> {
        return if (ServerVersion.HAS_KEYS) {
            org.bukkit.Sound.values().map { it.key.toString() }
        } else {
            try {
                val craftSound = ServerVersion.getCraftClass("CraftSound")
                val field = craftSound.getDeclaredField("minecraftKey")
                field.isAccessible = true

                craftSound.enumConstants.map { field.get(it) as String }
            } catch (e: ReflectiveOperationException) {
                exception("Could not retrieve sounds", e)
                emptyList()
            }
        }
    }

}