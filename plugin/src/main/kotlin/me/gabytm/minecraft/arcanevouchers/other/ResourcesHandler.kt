package me.gabytm.minecraft.arcanevouchers.other

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.info
import me.gabytm.minecraft.arcanevouchers.functions.warning
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
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ResourcesHandler(plugin: ArcaneVouchers) {

    private val resourcesFolder = File(plugin.dataFolder, ".RESOURCES")
    private val time = SimpleDateFormat("dd MMM yyyy, HH:mm").format(Date())

    init {
        resourcesFolder.mkdirs()
        resourcesFolder.listFiles()?.forEach { it.delete() }

        if (File(plugin.dataFolder, ".UTILS.yml").delete()) {
            warning(".UTILS.yml was deleted, check the new files located in .RESOURCES")
        }

        create("BossBar") {
            it["colors"] = BossBar.Color.NAMES.keys().toList().sorted()
            it["flags"] = BossBar.Flag.NAMES.keys().toList().sorted()
            it["overlays"] = BossBar.Overlay.NAMES.keys().toList()
            it["progress.min"] = BossBar.MIN_PROGRESS
            it["progress.max"] = BossBar.MAX_PROGRESS
        }

        create("Colors") {
            it["list"] = Constant.NAMED_COLORS.keys.toList().sorted()
        }

        create("DyeColors") { yaml ->
            yaml["list"] = DyeColor.values().map { it.name }.sorted()
        }

        // For some reason, on versions <= 1.12.2, the first value of the array is null (???)
        create("Effects") { yaml ->
            yaml["types"] = PotionEffectType.values().filterNotNull().map { it.name }.sorted()
        }

        create("Enchantments") { yaml ->
            yaml["list"] = if (ServerVersion.HAS_KEYS) {
                Enchantment.values().map { it.key.toString() }
            } else {
                Enchantment.values().map { it.name }
            }.sorted()
        }

        create("ItemFlags") { yaml ->
            yaml["list"] = ItemFlag.values().map { it.name }.sorted()
        }

        create("Materials") {
            it["list"] = getMaterials()
        }

        // FIXME: find a way to read the pattern types, spigot defined the class as
        //     public interface PatternType extends OldEnum<PatternType>, Keyed
        //     but paper is still using an enum in 1.21 🤷‍♂️
        /*create("PatternTypes") { yaml ->
            yaml["list"] = PatternType.values().map { it.name }
        }*/

        create(
            "Sounds",
            "The sounds are formatted as '<Bukkit name> <Minecraft key>' for an easier search, the plugin accepts only the Minecraft key"
        ) {
            it["sounds"] = getSounds()
            it["sources"] = Sound.Source.NAMES.keys().sorted().toList()
        }
    }

    private fun create(fileName: String, comment: String = "", action: (YamlConfiguration) -> Unit) {
        val file = File(resourcesFolder, "$fileName.yml")

        try {
            file.createNewFile()
        } catch (e: IOException) {
            exception("Could not create $file", e)
        }

        val yaml = YamlConfiguration.loadConfiguration(file)
        yaml.options()
            .header("\nHelpful lists of data generated for Minecraft ${Bukkit.getBukkitVersion()} at $time\n$comment\n ")
        action(yaml)

        try {
            yaml.save(file)
        } catch (e: IOException) {
            exception("Could not save $file", e)
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

    /**
     * Equivalent of [Material.isItem] for legacy versions
     * @return whether the material is an item or not
     * @author MD_5 [original](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Material.java?until=7eb6b52fb21699805eab4b074599030861227e64#1397-1455)
     */
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

    /**
     * Get all the sounds available in the current version formatted as `<Bukkit Name> <Minecraft Key>`
     */
    private fun getSounds(): List<String> {
        return if (ServerVersion.HAS_KEYS) {
            org.bukkit.Sound.values().map { sound -> "$sound ${sound.key}" }
        } else if (ServerVersion.HAS_OFF_HAND) {
            // On 1.9 - 1.12.2, each Sound has a minecraftKey field
            try {
                val craftSound = ServerVersion.getCraftClass("CraftSound")
                val minecraftKeyField = craftSound.getDeclaredField("minecraftKey")
                minecraftKeyField.isAccessible = true

                craftSound.enumConstants.map { sound -> "$sound ${minecraftKeyField.get(sound) as String}" }
            } catch (e: ReflectiveOperationException) {
                exception("Could not retrieve sounds", e)
                emptyList()
            }
        } else {
            // On 1.8, the keys are stored on a string array
            try {
                val craftSound = ServerVersion.getCraftClass("CraftSound")
                val soundsField = craftSound.getDeclaredField("sounds")
                soundsField.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val keys = soundsField.get(null) as Array<String>
                org.bukkit.Sound.values().mapIndexed { i, sound -> "$sound ${keys[i]}" }
            } catch (e: ReflectiveOperationException) {
                exception("Could not retrieve sounds", e)
                emptyList()
            }
        }
    }

}