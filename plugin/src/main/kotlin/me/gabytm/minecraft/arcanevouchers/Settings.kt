package me.gabytm.minecraft.arcanevouchers

import org.bukkit.configuration.file.FileConfiguration

class Settings(config: FileConfiguration) {

    var disableCrafting: Boolean = false; private set

    init {
        load(config)
    }

    fun load(config: FileConfiguration) {
        this.disableCrafting = config.getBoolean("settings.disable.crafting")
    }

}