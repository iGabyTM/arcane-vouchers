package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.Settings
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

private val plugin = JavaPlugin.getPlugin(ArcaneVouchers::class.java)
private val logger = plugin.logger

fun debug(player: Player? = null, message: String) {
    if (plugin.settings.debug) {
        if (player == null) {
            logger.info("[DEBUG] $message")
        } else {
            logger.info("[DEBUG, ${player.name}] $message")
        }
    }
}

fun exception(message: String, exception: Throwable) = logger.log(Level.SEVERE, "$message (${ServerVersion.CURRENT})", exception)

fun info(message: String) = logger.info(message)

fun warning(message: String) = logger.warning(message)