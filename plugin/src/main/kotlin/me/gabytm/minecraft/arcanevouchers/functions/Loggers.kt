package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

private val logger = JavaPlugin.getPlugin(ArcaneVouchers::class.java).logger

fun exception(message: String, exception: Throwable) = logger.log(Level.SEVERE, message, exception)

fun info(message: String) = logger.info(message)

fun warning(message: String) = logger.warning(message)