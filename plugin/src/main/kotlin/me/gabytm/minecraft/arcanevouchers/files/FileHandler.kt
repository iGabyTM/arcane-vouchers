package me.gabytm.minecraft.arcanevouchers.files

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.exception
import java.io.IOException
import java.nio.file.Files

class FileHandler(private val plugin: ArcaneVouchers) {

    init {
        saveFile("config_legacy.yml", "config.yml")
        saveFile("vouchers_legacy.yml", "vouchers.yml")
    }

    private fun saveFile(legacyName: String, name: String) {
        val file = plugin.dataFolder.resolve(name)

        if (file.exists()) {
            return
        }

        try {
            if (ServerVersion.IS_LEGACY) {
                plugin.saveResource(legacyName, false)
                Files.move(plugin.dataFolder.toPath().resolve(legacyName), file.toPath())
            } else {
                plugin.saveResource(name, false)
            }
        } catch (e: IOException) {
            if (ServerVersion.IS_LEGACY) {
                exception("Could not save $legacyName", e)
            } else {
                exception("Could not save $name", e)
            }
        }
    }

}