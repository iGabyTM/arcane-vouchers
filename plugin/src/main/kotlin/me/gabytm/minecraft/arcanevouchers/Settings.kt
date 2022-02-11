package me.gabytm.minecraft.arcanevouchers

import me.gabytm.minecraft.arcanevouchers.functions.parseTime
import org.bukkit.configuration.file.FileConfiguration
import java.util.concurrent.TimeUnit

class Settings(config: FileConfiguration) {

    var debug: Boolean = false; private set

    var disableCrafting: Boolean = false; private set
    var cooldownSaveThreshold: Long = 30L; private set

    init {
        load(config)
    }

    fun load(config: FileConfiguration) {
        this.debug = config.getBoolean("settings.DEBUG")
        this.disableCrafting = config.getBoolean("settings.disable.crafting")
        this.cooldownSaveThreshold = (config.getString("settings.cooldownSaveThreshold") ?: "").parseTime(TimeUnit.MILLISECONDS)
    }

}