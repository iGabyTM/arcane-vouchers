package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

private val audiences = JavaPlugin.getPlugin(ArcaneVouchers::class.java).audiences

fun CommandSender.audience(): Audience = audiences.sender(this)