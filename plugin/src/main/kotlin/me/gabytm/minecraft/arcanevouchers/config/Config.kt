package me.gabytm.minecraft.arcanevouchers.config

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.exception
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.nio.file.Files

class Config(plugin: ArcaneVouchers, path: String, isResource: Boolean = true) {

    private val file = File(plugin.dataFolder, path)
    var yaml: YamlConfiguration
        private set

    init {
        if (!Files.exists(file.toPath())) {
            if (isResource) {
                plugin.saveResource(path, false)
            } else {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    exception("Could not create ${file.path}", e)
                }
            }
        }

        this.yaml = YamlConfiguration.loadConfiguration(file)
    }

    fun reload() {
        this.yaml = YamlConfiguration.loadConfiguration(file)
    }

    fun getSection(path: String): ConfigurationSection? = yaml.getConfigurationSection(path)

}